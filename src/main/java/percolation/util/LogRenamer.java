package percolation.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;

public class LogRenamer {
    public static void main(String[] args) throws IOException {
        File file = new File("log/run.log");
        if (file.exists() && file.length() > 0) {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            String creationTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(attrs.creationTime().toMillis());
            File renamedFile = new File("log/run-" + creationTime + ".log");
            Files.move(file.toPath(), renamedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Renamed log file to " + renamedFile.getName());
        }
    }
}
