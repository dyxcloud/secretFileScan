package cnpat.filescan;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelFileUtil;
import cn.hutool.poi.excel.sax.Excel03SaxReader;
import cn.hutool.poi.excel.sax.Excel07SaxReader;
import cn.hutool.poi.excel.sax.ExcelSaxReader;
import cn.hutool.poi.excel.sax.handler.RowHandler;
import cn.hutool.poi.exceptions.POIException;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
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
        ZipSecureFile.setMinInflateRatio(-1.0d);//poi解压比例限制
    }

    public void check(File f) {
        //判断文件名
        CheckResult checkName = checkName(f);
        if (checkName.isRisk()) {
            Program.print("发现风险文件:"+ f.getAbsolutePath());
            Program.resultAppender.append(checkName, f);
            return;
        }
        //判断文件内容
        String suffix = FileUtil.getSuffix(f);
        Function<File, CheckResult> func = funcMap.get(suffix);
        if (func == null) {
            return;
        }
        log.info("parse file:" + f.getAbsolutePath());
        CheckResult apply = func.apply(f);
        if (apply.isRisk()) {
            Program.print("发现风险文件:"+ f.getAbsolutePath());
            Program.resultAppender.append(apply, f);
        }
    }

    public CheckResult checkName(File f) {
        return rule.checkTest(f.getName(), CheckResult.CheckContentFileName);
    }

    public CheckResult checkDoc(File f) {
        if(f.length()>100*1024*1024){
            log.info("ignore file:"+f.getAbsolutePath());
            return CheckResult.IGNORE;
        }
        try (FileInputStream fis = new FileInputStream(f);
             HWPFDocument doc = new HWPFDocument(fis)) {
            WordExtractor wordExtractor = new WordExtractor(doc);
            String text = wordExtractor.getText();
            String[] split = text.split("[\r\n]");
            for (String s : split) {
                CheckResult checkResult = rule.checkTest(s);
                if (checkResult.isRisk()) {
                    return checkResult;
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
        if(f.length()>100*1024*1024){
            log.info("ignore file:"+f.getAbsolutePath());
            return CheckResult.IGNORE;
        }
        try (FileInputStream fis = new FileInputStream(f);
             XWPFDocument doc = new XWPFDocument(fis)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            String text = extractor.getText();
            String[] split = text.split("[\r\n]");
            for (String s : split) {
                CheckResult checkResult = rule.checkTest(s);
                if (checkResult.isRisk()) {
                    return checkResult;
                }
            }
        } catch (Exception e) {
            log.error("读取文件失败: " + f.getAbsolutePath(), e);
        }
        return CheckResult.PASS;
    }

    public CheckResult checkXls(File f) {
        if (f.length() > 10 * 1024 * 1024) {
            log.info("ignore file:" + f.getAbsolutePath());
            return CheckResult.IGNORE;
        }
        if (f.getName().startsWith("~$") && f.length() < 500) {
            return CheckResult.PASS;
        }
        try (FileInputStream in = new FileInputStream(f);
             HSSFWorkbook workbook = new HSSFWorkbook(in);) {
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

    public CheckResult checkXlsx(File f) {
        if (f.length() > 10 * 1024 * 1024) {
            log.info("ignore file:" + f.getAbsolutePath());
            return CheckResult.IGNORE;
        }
        if (f.getName().startsWith("~$") && f.length() < 500) {
            return CheckResult.PASS;
        }
        try (FileInputStream in = new FileInputStream(f);
             XSSFWorkbook workbook = new XSSFWorkbook(in);) {
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
            if(f.length()>10*1024*1024){
                log.info("ignore file:"+f.getAbsolutePath());
                return CheckResult.IGNORE;
            }
            if (f.getName().startsWith("~$") && f.length() < 500) {
                return CheckResult.PASS;
            }
            // ExcelUtil.
            readBySax(f, -1, (sheetIndex, rowIndex, rowList) -> {
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
            if (e.getMessage().contains("sax扫描中命中关键字")) {
                return data[0];
            } else {
                log.error("读取文件失败: " + f.getAbsolutePath(), e);
            }
        }
        return CheckResult.PASS;
    }

    public CheckResult checkPDF(File f) {
        if(f.length()>100*1024*1024){
            log.info("ignore file:"+f.getAbsolutePath());
            return CheckResult.IGNORE;
        }
        try (PDDocument pdDocument = PDDocument.load(f, MemoryUsageSetting.setupTempFileOnly());) {
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
        } catch (Exception e) {
            log.error("读取文件失败: " + f.getAbsolutePath(), e);
        }
        return CheckResult.PASS;
    }


    public void readBySax(File file, int rid, RowHandler rowHandler) throws POIException {
        final ExcelSaxReader<?> reader = ExcelFileUtil.isXlsx(file)
                ? new Excel07SaxReader2(rowHandler)
                : new Excel03SaxReader2(rowHandler);
        reader.read(file, rid);
    }

    static class Excel07SaxReader2 extends Excel07SaxReader {
        public Excel07SaxReader2(RowHandler rowHandler) {
            super(rowHandler);
        }
        @Override
        public Excel07SaxReader read(File file, String idOrRidOrSheetName) throws POIException {
            try (OPCPackage open = OPCPackage.open(file, PackageAccess.READ);){
                return read(open, idOrRidOrSheetName);
            } catch (InvalidFormatException | IOException e) {
                throw new POIException(e);
            }
        }
    }

    static class Excel03SaxReader2 extends Excel03SaxReader {
        public Excel03SaxReader2(RowHandler rowHandler) {
            super(rowHandler);
        }
        @Override
        public Excel03SaxReader read(File file, String idOrRidOrSheetName) throws POIException {
            try (POIFSFileSystem poifsFileSystem = new POIFSFileSystem(file, true);) {
                return read(poifsFileSystem, idOrRidOrSheetName);
            } catch (IOException e) {
                throw new POIException(e);
            }
        }
    }
    
}
