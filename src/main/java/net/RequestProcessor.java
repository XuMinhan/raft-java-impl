package net;

import io.netty.channel.ChannelHandlerContext;
import net.RPC.entity.RemotingRequest;
import net.RPC.entity.RemotingResponse;

public interface RequestProcessor {
    RemotingResponse processRequest(ChannelHandlerContext ctx, RemotingRequest request) throws Exception;
}
