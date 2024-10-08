package net.Kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import net.RPC.entity.NodeIdAndOqId;
import net.RPC.entity.RemotingRequest;
import net.RPC.entity.RemotingResponse;
import net.RPC.entity.SingleLog;

public class KryoSerializer {

    // 使用 ThreadLocal 确保每个线程有独立的 Kryo 实例
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RemotingRequest.class);
        kryo.register(RemotingResponse.class);
        kryo.register(SingleLog.class);
        kryo.register(NodeIdAndOqId.class);
        return kryo;
    });

    public static byte[] serialize(Object object) {
        Kryo kryo = kryoThreadLocal.get();
        Output output = new Output(4096);
        try {
            kryo.writeObject(output, object);
            return output.toBytes();
        } finally {
            output.close(); // 确保 Output 被关闭以释放资源
        }
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz) {
        Kryo kryo = kryoThreadLocal.get();
        Input input = new Input(data);
        try {
            return kryo.readObject(input, clazz);
        } finally {
            input.close(); // 确保 Input 被关闭以释放资源
        }
    }
}
