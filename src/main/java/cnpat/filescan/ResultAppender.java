package cnpat.filescan;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ResultAppender {

    String userName;
    String pcName;
    String checkDateTime;
    Map<String,int[]> countMap = new HashMap<>();
    Map<Integer,File> fileMap = new HashMap<>();

    public ResultAppender(String userName, String pcName, String checkDateTime) {
        this.userName = userName;
        this.pcName = pcName;
        this.checkDateTime = checkDateTime;
        String path = userName + "-" + pcName + "-" + checkDateTime;
        File ff;
        ff = new File(path + "\\ignore.log");
        FileUtil.touch(ff);
        fileMap.put(CheckResult.Ignore,ff);
        ff = new File(path + "\\info.log");
        FileUtil.touch(ff);
        fileMap.put(CheckResult.Info,ff);
        ff = new File(path + "\\warn.log");
        FileUtil.touch(ff);
        fileMap.put(CheckResult.Warn,ff);
        ff = new File(path + "\\error.log");
        FileUtil.touch(ff);
        fileMap.put(CheckResult.Error,ff);
    }

    public void append(CheckResult result, File file) {
        String absolutePath = file.getAbsolutePath();
        String root = absolutePath.substring(0, absolutePath.indexOf(":\\") + 2);
        countMap.get(root)[1]++;
        FileAppender appender = new FileAppender(fileMap.get(result.getResult()), 16, true);
        //计算机名***级别***检查点***检查内容***匹配内容***文件路径
        String format = StrUtil.format("{}***{}***{}***{}***{}***{}"
                , pcName, result.getResult(), result.getCheckPoint(), result.getCheckContent()
                , result.getCheckMessage().replace("\r","").replace("\n","")
                , file.getAbsolutePath());
        appender.append(format);
        appender.flush();
    }
    
}
