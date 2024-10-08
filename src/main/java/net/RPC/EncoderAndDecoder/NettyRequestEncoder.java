package net.RPC.EncoderAndDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.Kryo.KryoSerializer;
import net.RPC.entity.RemotingRequest;

public class NettyRequestEncoder extends MessageToByteEncoder<RemotingRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RemotingRequest request, ByteBuf out) throws Exception {
        byte[] data = KryoSerializer.serialize(request);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
