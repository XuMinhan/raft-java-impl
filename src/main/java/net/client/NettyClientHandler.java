package net.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.RPC.entity.RemotingResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NettyClientHandler extends SimpleChannelInboundHandler<RemotingResponse> {
    private static final Logger netLogger = LogManager.getLogger("net");

    private final RemotingClient remotingClient;

    public NettyClientHandler(RemotingClient remotingClient) {
        this.remotingClient = remotingClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RemotingResponse response) throws Exception {

        remotingClient.processResponse(ctx, response);
    }

}

