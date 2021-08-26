import cnpat.filescan.DiskWalker;
import org.junit.jupiter.api.Test;

import java.io.File;

public class DiskWalkerTest {
    
    DiskWalker walker = new DiskWalker();
    
    @Test
    public void walkPath(){
        walker.walkPath(new File("C:\\")).forEach(f-> System.out.println(f.getName()));
    }
    @Test
    public void listRoots(){
        walker.getRoots().forEach(f-> System.out.println(f.getPath()));
    }
}
