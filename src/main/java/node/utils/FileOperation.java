package node.utils;

import java.io.File;

public class FileOperation {
    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }
}
