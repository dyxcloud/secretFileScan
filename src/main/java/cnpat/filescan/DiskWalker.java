package cnpat.filescan;

import cn.hutool.core.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DiskWalker {

    final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 磁盘类型
     * http://www.cfan.com.cn/2021/0608/135249.shtml
     */
    private final static List<String> whiteType = Collections.singletonList("本地磁盘");

    public List<File> walkPath(File path) {
        return loopFiles(path.toPath(), -1, null);
    }

    public List<File> getRoots() {
        File[] roots = File.listRoots();
        FileSystemView sys = FileSystemView.getFileSystemView();
        return Arrays.stream(roots)
                // .peek(f -> System.out.println(sys.getSystemTypeDescription(f)))
                .filter(f -> whiteType.contains(sys.getSystemTypeDescription(f)))
                .collect(Collectors.toList());
    }

    public List<File> loopFiles(Path path, int maxDepth, FileFilter fileFilter) {
        final List<File> fileList = new ArrayList<>();
        if (null == path || !Files.exists(path)) {
            return fileList;
        } else if (!FileUtil.isDirectory(path)) {
            final File file = path.toFile();
            if (null == fileFilter || fileFilter.accept(file)) {
                fileList.add(file);
            }
            return fileList;
        }
        FileUtil.walkFiles(path, maxDepth, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                final File file = path.toFile();
                if (null == fileFilter || fileFilter.accept(file)) {
                    fileList.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                try {
                    return super.visitFileFailed(file, exc);
                } catch (AccessDeniedException ade) {
                    log.error("Unable to access files in:" + file);
                    return FileVisitResult.CONTINUE;
                }
            }
        });
        return fileList;
    }


}
