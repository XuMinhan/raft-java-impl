package net.server;

import io.netty.channel.ChannelHandlerContext;
import net.RPC.entity.RemotingRequest;
import net.RPC.entity.RemotingResponse;
import net.RequestProcessor;
import node.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static net.RPC.entity.RPCKind.rpcKindMap;

public class RemotingServer {
    Node node;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    private static final Logger netLogger = LogManager.getLogger("net");

    private final ConcurrentMap<Integer, RequestProcessor> processorTable = new ConcurrentHashMap<>();

    // 注册处理器
    public void registerProcessor(int requestCode, RequestProcessor processor) {
        processorTable.put(requestCode, processor);
    }

    // 处理请求
    public void processRequest(ChannelHandlerContext ctx, RemotingRequest request) throws Exception {
        netLogger.trace("节点"+node.getNodeInfo().getId()+
                "处理"+request.getCommandFromId()+"节点发送的rpc类型为"+
                rpcKindMap.get(request.getRpcCommandKind())+"的请求");
        RequestProcessor processor = processorTable.get(request.getKind());
        if (processor != null) {
            RemotingResponse response = processor.processRequest(ctx, request);
            if (response != null) {
                ctx.writeAndFlush(response);
            }
        } else {
            System.err.println("No processor found for request code: " + request.getKind());
        }
    }
}
