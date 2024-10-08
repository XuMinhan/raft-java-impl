package net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.RPC.EncoderAndDecoder.NettyRequestEncoder;
import net.RPC.EncoderAndDecoder.NettyResponseDecoder;

import java.util.concurrent.ConcurrentHashMap;

public class NettyClient {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroupWorker;
    private final RemotingClient remotingClient;


    public NettyClient(RemotingClient remotingClient) {
        this.remotingClient = remotingClient;
        this.bootstrap = new Bootstrap();
        remotingClient.setBootstrap(bootstrap);
        this.eventLoopGroupWorker = new NioEventLoopGroup();
    }

    public void start() {
        bootstrap.group(eventLoopGroupWorker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new NettyResponseDecoder(), new NettyRequestEncoder(), new NettyClientHandler(remotingClient));
                    }
                });
    }

    public void testConnect(String host, int port) throws InterruptedException {
        ChannelFuture future = bootstrap.connect(host, port).sync();
        if (future.isSuccess()) {
            this.remotingClient.setChannel(future.channel());
        } else {
            throw new RuntimeException("Connection failed");
        }
    }

    public void shutdown() {
        eventLoopGroupWorker.shutdownGracefully();
    }
}
