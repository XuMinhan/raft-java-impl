package net.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.RPC.EncoderAndDecoder.NettyRequestDecoder;
import net.RPC.EncoderAndDecoder.NettyResponseEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NettyServer {

    private final int port;
    private final RemotingServer remotingServer;

    // 将 bossGroup 和 workerGroup 声明为类的成员变量
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer(int port, RemotingServer remotingServer) {
        this.port = port;
        this.remotingServer = remotingServer;
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyRequestDecoder(), new NettyResponseEncoder(), new NettyServerHandler(remotingServer));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            shutdown();
        }
    }

    // 优雅地关闭服务器
    public void shutdown() {
        if (workerGroup != null && !workerGroup.isShutdown()) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null && !bossGroup.isShutdown()) {
            bossGroup.shutdownGracefully();
        }
    }
}
