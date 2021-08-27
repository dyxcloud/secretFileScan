package cnpat.filescan;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Program {
    static final Logger log = LoggerFactory.getLogger(Program.class);

    private static final DiskWalker diskWalker = new DiskWalker();
    private static final FileChecker fileChecker = new FileChecker();
    public static ResultAppender resultAppender;
    
    static {
        //禁用pdfbox的警告console
        System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.NoOpLog");
        // java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.OFF);
    }
    
    public static void main(String[] args) {
        //交互录入信息
        print("请输入PC所有人姓名:");
        Scanner scanner = new Scanner(System.in);
        String userName = scanner.next();
        resultAppender = new ResultAppender(userName
                , System.getenv().get("COMPUTERNAME")
                , DateUtil.formatLocalDateTime(LocalDateTime.now()).replace(':', '_'));
        List<File> roots = diskWalker.getRoots();
        // List<File> roots = Collections.singletonList(new File("F:\\"));
        for (File root : roots) {
            try {
                print("索引" + root + "盘文件...");
                List<File> files = diskWalker.walkPath(root);
                int diskSize = files.size();
                String rootPath = root.getPath();
                rootPath = rootPath.substring(0, rootPath.indexOf(":\\") + 2);
                resultAppender.countMap.put(rootPath, new int[]{diskSize, 0});
                print(">>>>>>>>>开始扫描磁盘:" + root.getPath()+" 文件数量:"+diskSize);
                final double unit = 0.1;
                double perCent = unit;
                int time=1;
                for(int i=0;i<diskSize;i++){
                    if (i >= diskSize * perCent) {
                        print(StrUtil.format("已完成{}0% 文件进度: {}/{}", time, i, diskSize));
                        perCent += unit;
                        time++;
                    }
                    int index = diskSize - i - 1;
                    File f = files.remove(index);
                    try {
                        fileChecker.check(f);
                    } catch (Exception e) {
                        log.error("check error",e);
                    }
                }
            } catch (Exception e) {
                log.error("walkPath error",e);
            }
        }
        print(">>>>>>>>>扫描完成");
        Map<String,int[]> countMap = resultAppender.countMap;
        for (Map.Entry<String, int[]> entry : countMap.entrySet()) {
            String format = StrUtil.format(">>>{}盘扫描文件{}个, 发现异常文件{}个", entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
            print(format);
        }
    }
    
    public static void print(String s){
        System.out.println(s);
        log.info(s);
    }
}
