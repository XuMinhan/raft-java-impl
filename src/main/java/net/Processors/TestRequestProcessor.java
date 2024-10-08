package net.Processors;

import io.netty.channel.ChannelHandlerContext;
import net.RPC.entity.NodeIdAndOqId;
import net.RPC.entity.RemotingRequest;
import net.RPC.entity.RemotingResponse;
import net.RequestProcessor;
import node.Node;

public class TestRequestProcessor implements RequestProcessor {
    Node node;

    public TestRequestProcessor(Node node) {
        this.node = node;
    }

    @Override
    public RemotingResponse processRequest(ChannelHandlerContext ctx, RemotingRequest request) throws Exception {
        NodeIdAndOqId msgId = new NodeIdAndOqId(request.getCommandFromId(), request.getOpaque());
        RemotingResponse remotingResponse = new RemotingResponse(msgId);
        remotingResponse.setSuccess(true);
        remotingResponse.setMessage("TestSuccess");
        return remotingResponse;
    }
}
