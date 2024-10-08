package net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.Processors.ResponseCallback;
import net.RPC.entity.NodeIdAndOqId;
import net.RPC.entity.RemotingRequest;
import net.RPC.entity.RemotingResponse;
import net.RPC.entity.ResponseFuture;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static net.RPC.entity.RPCKind.RequestVote;

public abstract class AbstractRemoting {

    protected final ConcurrentMap<NodeIdAndOqId, ResponseFuture> responseTable = new ConcurrentHashMap<>(256);
    private final AtomicInteger requestIdGenerator = new AtomicInteger(0);

    // 处理收到的响应
    public void processResponse(ChannelHandlerContext ctx, RemotingResponse response) {
        NodeIdAndOqId msgId = response.getMsgId();
        //todo
//        System.out.println("接收"+msgId);


        ResponseFuture responseFuture = responseTable.get(msgId);
        if (responseFuture != null) {
            responseFuture.setSuccess(true);
            responseFuture.setResponse(response);
            responseFuture.getCountDownLatch().countDown();
            responseTable.remove(msgId);

            // 如果存在回调，执行回调处理
            if (responseFuture.getCallback() != null) {
                //**************************************************************

                responseFuture.getCallback().onResponse(response);
            }
        }
    }

    // 发送请求并等待响应
    public ResponseFuture syncSendRequest(Channel channel, RemotingRequest command) throws InterruptedException {
        // 使用 AtomicInteger 生成唯一的 opaque ID
        int opaque = requestIdGenerator.incrementAndGet();
        command.setOpaque(opaque);
        ResponseFuture responseFuture = new ResponseFuture(opaque, command);
        NodeIdAndOqId msgId = new NodeIdAndOqId(command.getCommandFromId(), opaque);
        responseTable.put(msgId, responseFuture);

        channel.writeAndFlush(command).addListener(future -> {
            if (!future.isSuccess()) {
                responseFuture.setSuccess(false);
                responseTable.remove(msgId);
            }
        });

        // 等待响应
        boolean awaitSuccess = responseFuture.getCountDownLatch().await(3, TimeUnit.SECONDS);
        if (!awaitSuccess) {
            responseTable.remove(opaque);
            throw new RuntimeException("Request timeout");
        }

        return responseFuture;
    }


    // 发送请求并设置回调
    public ResponseFuture syncSendRequest(Channel channel, RemotingRequest command, ResponseCallback callback) throws InterruptedException {
        // 使用 AtomicInteger 生成唯一的 opaque ID
        int opaque = requestIdGenerator.incrementAndGet();
        command.setOpaque(opaque);

        // 创建 ResponseFuture 并设置回调
        ResponseFuture responseFuture = new ResponseFuture(opaque, command);
        responseFuture.setCallback(callback);
        NodeIdAndOqId msgId = new NodeIdAndOqId(command.getCommandFromId(), opaque);
        responseTable.put(msgId, responseFuture);


        channel.writeAndFlush(command).addListener(future -> {
            if (!future.isSuccess()) {
                responseFuture.setSuccess(false);
                responseTable.remove(msgId);
                // 如果请求发送失败，调用失败的回调
                if (callback != null) {
                    NodeIdAndOqId sendId = new NodeIdAndOqId(command.getCommandFromId(), command.getOpaque());
                    RemotingResponse errorResponse = new RemotingResponse(sendId);
                    errorResponse.setSuccess(false);
                    callback.onResponse(errorResponse);
                }
            }
        });
        // 等待响应
        boolean awaitSuccess = responseFuture.getCountDownLatch().await(3, TimeUnit.SECONDS);
        if (!awaitSuccess) {
//            responseTable.remove(opaque);
            throw new RuntimeException("Request timeout");
        }
        return responseFuture;
    }


    public ResponseFuture asyncSendRequest(Channel channel, RemotingRequest command, ResponseCallback callback) throws InterruptedException {
        // 使用 AtomicInteger 生成唯一的 opaque ID
        int opaque = requestIdGenerator.incrementAndGet();
        command.setOpaque(opaque);


        // 创建 ResponseFuture 并设置回调
        ResponseFuture responseFuture = new ResponseFuture(opaque, command);
        responseFuture.setCallback(callback);
        NodeIdAndOqId msgId = new NodeIdAndOqId(command.getCommandFromId(), opaque);
        //**************************************************************

        responseTable.put(msgId, responseFuture);

        channel.writeAndFlush(command).addListener(future -> {
            if (!future.isSuccess()) {
                responseFuture.setSuccess(false);
                responseTable.remove(msgId);
                // 如果请求发送失败，调用失败的回调
                if (callback != null) {
                    NodeIdAndOqId sendId = new NodeIdAndOqId(command.getCommandFromId(), command.getOpaque());
                    RemotingResponse errorResponse = new RemotingResponse(sendId);
                    errorResponse.setSuccess(false);
                    callback.onResponse(errorResponse);
                }
            }
        });
        return null;
    }
}
