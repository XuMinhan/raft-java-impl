package net.server;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.RPC.entity.RemotingRequest;

public class NettyServerHandler extends SimpleChannelInboundHandler<RemotingRequest> {
    private final RemotingServer remotingServer;

    public NettyServerHandler(RemotingServer remotingServer) {
        this.remotingServer = remotingServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RemotingRequest request) throws Exception {
        remotingServer.processRequest(ctx, request);
    }
}
