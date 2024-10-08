package net.RPC.EncoderAndDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.Kryo.KryoSerializer;
import net.RPC.entity.RemotingResponse;

public class NettyResponseDecoder extends LengthFieldBasedFrameDecoder {
    public NettyResponseDecoder() {
        super(16777216, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        try {
            byte[] data = new byte[frame.readableBytes()];
            frame.readBytes(data);
            return KryoSerializer.deserialize(data, RemotingResponse.class);
        } finally {
            frame.release();  // 确保释放 ByteBuf，以避免内存泄漏
        }
    }

}

