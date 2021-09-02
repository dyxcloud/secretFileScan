package cnpat.filescan;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelUtil;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FileChecker {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    Rule rule = new Rule();

    Map<String, Function<File, CheckResult>> funcMap = new HashMap<>();

    {
        funcMap.put("doc", this::checkDoc);
        funcMap.put("docx", this::checkDocx);
        funcMap.put("xls", this::checkExcel);
        funcMap.put("xlsx", this::checkExcel);
        funcMap.put("pdf", this::checkPDF);
    }

    public void check(File f) {
        //判断文件名
        CheckResult checkName = checkName(f);
        if (checkName.isRisk()) {
            Program.print("发现风险文件:" + f.getAbsolutePath());
            Program.resultAppender.append(checkName, f);
            return;
        }
        //判断文件内容
        String suffix = FileUtil.getSuffix(f).toLowerCase();
        Function<File, CheckResult> func = funcMap.get(suffix);
        if (func == null) {
            return;
        }
        log.info("parse file:" + f.getAbsolutePath());
        CheckResult apply = func.apply(f);
        if (apply.isRisk()) {
            Program.print("发现风险文件:" + f.getAbsolutePath());
            Program.resultAppender.append(apply, f);
        }
    }

    public CheckResult checkName(File f) {
        return rule.checkTest(f.getName(), CheckResult.CheckContentFileName);
    }

    /**
     * 文件过大返回true
     */
    public boolean isLargeSize(File f) {
        String suffix = FileUtil.getSuffix(f).toLowerCase();
        switch (suffix) {
            case "doc":
                return f.length() > 90 * 1024 * 1024;
            case "docx":
                if (f.length() > 90 * 1024 * 1024) {
                    return true;
                } else {
                    // ZipSecureFile.setMinInflateRatio(0.03d);//poi解压比例限制
                    ZipSecureFile.setMaxEntrySize(80 * 1024 * 1024L);//原始文件大小超过80M读取时会报错
                    return false;
                }
            case "xls":
                return f.length() > 10 * 1024 * 1024;
            case "xlsx":
                //excel使用sax读取,暂不做解压过滤, 设置为默认值
                // ZipSecureFile.setMinInflateRatio(0.01d);
                ZipSecureFile.setMaxEntrySize(0xFFFFFFFFL);
                return f.length() > 10 * 1024 * 1024;
            case "pdf":
                return f.length() > 100 * 1024 * 1024;
            default:
                return false;
        }
    }

    public CheckResult checkDoc(File f) {
        if (isLargeSize(f)) {
            log.info("ignore file:" + f.getAbsolutePath());
            return CheckResult.IGNORE;
        }
        try (FileInputStream fis = new FileInputStream(f);
             HWPFDocument doc = new HWPFDocument(fis)) {
            WordExtractor wordExtractor = new WordExtractor(doc);
            String[] paragraphText = wordExtractor.getParagraphText();
            for (String text : paragraphText) {
                String[] split = text.split("[\r\n]");
                for (String s : split) {
                    CheckResult checkResult = rule.checkTest(s);
                    if (checkResult.isRisk()) {
                        return checkResult;
                    }
                }
            }   
        } catch (Exception e) {
            if ("The document is really a OOXML file".equals(e.getMessage())) {
                return checkDocx(f);
            } else {
                log.error("读取文件失败: " + f.getAbsolutePath(), e);
            }
        }
        return CheckResult.PASS;
    }

    public CheckResult checkDocx(File f) {
        if (isLargeSize(f)) {
            log.info("ignore file:" + f.getAbsolutePath());
            return CheckResult.IGNORE;
        }
        try (FileInputStream fis = new FileInputStream(f);
             XWPFDocument doc = new XWPFDocument(fis)) {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            for (XWPFParagraph para : paragraphs) {
                String text = para.getText();
                String[] split = text.split("[\r\n]");
                for (String s : split) {
                    CheckResult checkResult = rule.checkTest(s);
                    if (checkResult.isRisk()) {
                        return checkResult;
                    }
                }
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Zip bomb detected")) {
                return CheckResult.IGNORE;
            } else {
                log.error("读取文件失败: " + f.getAbsolutePath(), e);
            }
        }
        return CheckResult.PASS;
    }

    @Deprecated
    public CheckResult checkXls(File f) {
        if (f.getName().startsWith("~$") && f.length() < 500) {
            return CheckResult.PASS;
        }
        if (isLargeSize(f)) {
            log.info("ignore file:" + f.getAbsolutePath());
            return CheckResult.IGNORE;
        }
        try (FileInputStream in = new FileInputStream(f);
             HSSFWorkbook workbook = new HSSFWorkbook(in)) {
            HSSFSheet sheet;
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {//获取每个Sheet表
                sheet = workbook.getSheetAt(i);
                for (int j = 0; j < sheet.getPhysicalNumberOfRows(); j++) {//获取每行
                    HSSFRow row = sheet.getRow(j);
                    for (int k = 0; k < row.getPhysicalNumberOfCells(); k++) {//获取每个单元格
                        HSSFCell cell = row.getCell(k);
                        if (cell == null) continue;
                        CheckResult checkResult = rule.checkTest(cell.toString());
                        if (checkResult.isRisk()) {
                            return checkResult;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("读取文件失败: " + f.getAbsolutePath(), e);
        }
        return CheckResult.PASS;
    }

    @Deprecated
    public CheckResult checkXlsx(File f) {
        if (f.getName().startsWith("~$") && f.length() < 500) {
            return CheckResult.PASS;
        }
        if (isLargeSize(f)) {
            log.info("ignore file:" + f.getAbsolutePath());
            return CheckResult.IGNORE;
        }
        try (FileInputStream in = new FileInputStream(f);
             XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            XSSFSheet sheet;
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {//获取每个Sheet表
                sheet = workbook.getSheetAt(i);
                for (int j = 0; j < sheet.getPhysicalNumberOfRows(); j++) {//获取每行
                    XSSFRow row = sheet.getRow(j);
                    for (int k = 0; k < row.getPhysicalNumberOfCells(); k++) {//获取每个单元格
                        XSSFCell cell = row.getCell(k);
                        if (cell == null) continue;
                        CheckResult checkResult = rule.checkTest(cell.toString());
                        if (checkResult.isRisk()) {
                            return checkResult;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("读取文件失败: " + f.getAbsolutePath(), e);
        }
        return CheckResult.PASS;
    }

    public CheckResult checkExcel(File f) {
        final CheckResult[] data = {null};
        try {
            if (f.getName().startsWith("~$") && f.length() < 500) {
                return CheckResult.PASS;
            }
            if (isLargeSize(f)) {
                log.info("ignore file:" + f.getAbsolutePath());
                return CheckResult.IGNORE;
            }
            ExcelUtil.readBySax(f, -1, (sheetIndex, rowIndex, rowList) -> {
                for (Object c : rowList) {
                    if (c == null) continue;
                    CheckResult checkResult = rule.checkTest(c.toString());
                    if (checkResult.isRisk()) {
                        data[0] = checkResult;
                        throw new RuntimeException("sax扫描中命中关键字");
                    }
                }
            });
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("sax扫描中命中关键字")) {
                return data[0];
            } else {
                log.error("读取文件失败: " + f.getAbsolutePath(), e);
            }
        }
        return CheckResult.PASS;
    }

    public CheckResult checkPDF(File f) {
        if (isLargeSize(f)) {
            log.info("ignore file:" + f.getAbsolutePath());
            return CheckResult.IGNORE;
        }
        try (PDDocument pdDocument = PDDocument.load(f, MemoryUsageSetting.setupTempFileOnly())) {
            int pages = pdDocument.getNumberOfPages();//获取总页码
            PDFTextStripper stripper = new PDFTextStripper();//获取文本内容
            stripper.setSortByPosition(true);//设置输出顺序（是否排序）
            for (int i = 1; i <= pages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String text = stripper.getText(pdDocument);
                String[] split = text.split("[\r\n]");
                for (String s : split) {
                    CheckResult checkResult = rule.checkTest(s);
                    if (checkResult.isRisk()) {
                        return checkResult;
                    }
                }
            }
        } catch (Throwable e) {
            log.error("读取文件失败: " + f.getAbsolutePath(), e);
        }
        return CheckResult.PASS;
    }

}
