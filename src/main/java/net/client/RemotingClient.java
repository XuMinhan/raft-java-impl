package net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import net.Processors.ResponseCallback;
import net.RPC.entity.RemotingRequest;
import net.RPC.entity.ResponseFuture;
import net.AbstractRemoting;
import node.Address;
import node.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

import static net.RPC.entity.RPCKind.rpcKindMap;

public class RemotingClient extends AbstractRemoting {
    private static final Logger netLogger = LogManager.getLogger("net");

    private Node node;
    private Channel testChannel;
    private Bootstrap bootstrap;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    private ConcurrentHashMap<Integer, Address> otherAddress;


    public void setChannel(Channel channel) {
        this.testChannel = channel;
    }

    public ResponseFuture testSend(RemotingRequest command) throws InterruptedException {

        return syncSendRequest(testChannel, command);
    }

    public ResponseFuture send(Integer nodeId, RemotingRequest command, ResponseCallback callback) throws InterruptedException {
        netLogger.trace("节点" + node.getNodeInfo().getId() +
                "同步向" + nodeId + "节点发送rpc类型为" +
                rpcKindMap.get(command.getRpcCommandKind()) + "的请求");

        Channel cn = getOrCreateChannel(nodeId);


        return syncSendRequest(cn, command, callback);
    }

    public ResponseFuture asyncSend(Integer nodeId, RemotingRequest command, ResponseCallback callback) throws InterruptedException {
        netLogger.trace("节点" + node.getNodeInfo().getId() +
                "异步向" + nodeId + "节点发送rpc类型为" +
                rpcKindMap.get(command.getRpcCommandKind()) + "的请求");
        Channel cn = getOrCreateChannel(nodeId);
        if (cn == null) {
            return null;
        }

        asyncSendRequest(cn, command, callback);
        return null;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public ConcurrentHashMap<Integer, Address> getOtherAddress() {
        return otherAddress;
    }

    public void setOtherAddress(ConcurrentHashMap<Integer, Address> otherAddress) {
        this.otherAddress = otherAddress;
    }

    private ConcurrentHashMap<Integer, Future<Channel>> channels = new ConcurrentHashMap<>();

    public Channel getOrCreateChannel(Integer nodeId) {
        // 尝试从缓存中获取连接的 Future
        Future<Channel> channelFuture = channels.get(nodeId);

        if (channelFuture == null || !isChannelActive(channelFuture)) {
            // 如果不存在有效的连接，则创建一个新的连接 FutureTask
            FutureTask<Channel> newChannelFutureTask = new FutureTask<>(() -> {
                Address address = otherAddress.get(nodeId);
                return bootstrap.connect(address.getHost(), address.getPort()).sync().channel();
            });

            // 使用原子操作 putIfAbsent，防止多个线程同时创建连接
            channelFuture = channels.putIfAbsent(nodeId, newChannelFutureTask);

            if (channelFuture == null) {
                // 如果此 Future 是第一次被放入缓存，那么执行创建连接的任务
                channelFuture = newChannelFutureTask;
                newChannelFutureTask.run();
            }
        }

        try {
            return channelFuture.get(); // 限定超时时间为 10 秒
        } catch (Exception e) {
            channels.remove(nodeId); // 其他异常情况下移除缓存
            netLogger.trace("节点" + nodeId + "掉线");
            return null;
//            throw new RuntimeException("Failed to create channel for nodeId: " + nodeId, e);
        }

    }

    // 判断 Future 中的 Channel 是否有效
    private boolean isChannelActive(Future<Channel> channelFuture) {
        try {
            return channelFuture != null && channelFuture.isDone() && channelFuture.get().isActive();
        } catch (Exception e) {
            return false;
        }
    }
}
