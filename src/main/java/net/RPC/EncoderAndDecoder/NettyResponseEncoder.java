package net.RPC.EncoderAndDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.Kryo.KryoSerializer;
import net.RPC.entity.RemotingResponse;

public class NettyResponseEncoder extends MessageToByteEncoder<RemotingResponse> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RemotingResponse response, ByteBuf out) throws Exception {
        byte[] data = KryoSerializer.serialize(response);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
