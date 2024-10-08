package net.Processors;

import io.netty.channel.ChannelHandlerContext;
import net.RPC.entity.RemotingRequest;
import net.RPC.entity.RemotingResponse;
import net.RequestProcessor;
import node.Node;

public class RequestFromClientProcessor implements RequestProcessor {
    Node node;

    public RequestFromClientProcessor(Node node) {
        this.node = node;
    }

    @Override
    public RemotingResponse processRequest(ChannelHandlerContext ctx, RemotingRequest request) throws Exception {
        return null;
    }
}
