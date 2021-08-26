import cn.hutool.core.io.FileUtil;
import cnpat.filescan.FileChecker;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;

public class FileCheckerTest {

    FileChecker fileChecker = new FileChecker();
    
    @Test
    public void checkWord(){
        // fileChecker.checkDoc(new File("C:\\WorkSpace\\Projects\\PIDP\\docs\\二期\\99-其它\\外观设计非专利文献数据库及检索系统\\可行性研究报告\\参考资料\\外观部提供\\2013年课题资料\\结题报告汇总表 （完整 ）2013.12.19终版.doc"));
        fileChecker.checkDocx(new File("D:\\OneDrive\\cnpat项目cloudera安装.docx"));
    }
    
    @Test
    public void checkExcel(){
        fileChecker.checkExcel(new File("D:\\Download\\应用与存储关联样例.xlsx"));
        // fileChecker.checkXlsx(new File("D:\\Download\\测试环境-业务系统安装清单列表.xlsx"));
        // fileChecker.checkExcel(new File("D:\\Download\\翻译平台\\10 系统档案\\虚机资源申请\\二期四台测试一台开发机申请文档\\业务系统安装清单列表20201204.xlsx"));
    }
    
    @Test
    public void checkPDF(){
        
        fileChecker.checkPDF(new File("D:\\OneDrive\\Develop\\books\\java\\effective java_3rdectioin.pdf"));
    }
    
    @Test
    public void tt(){
        String text ="qweqwe\r\naaaaa\rbbb\ncccc";
        System.out.println(Arrays.toString(text.split("[\r\n]")));
    }
}
