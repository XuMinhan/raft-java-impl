package node.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class AppendedObjectOutputStream extends ObjectOutputStream {
    public AppendedObjectOutputStream(FileOutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        // 重写方法以跳过写入流头，防止重复
        reset();
    }
}
