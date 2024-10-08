package net.Processors;

import io.netty.channel.ChannelHandlerContext;
import net.RPC.entity.NodeIdAndOqId;
import net.RPC.entity.RemotingRequest;
import net.RPC.entity.RemotingResponse;
import net.RequestProcessor;
import node.Node;

import static net.RPC.entity.RPCKind.HEART_BEAT;
import static net.RPC.entity.RPCKind.RequestVote;

public class RpcRequestProcessor implements RequestProcessor {
    Node node;

    public RpcRequestProcessor(Node node) {
        this.node = node;
    }

    @Override
    public RemotingResponse processRequest(ChannelHandlerContext ctx, RemotingRequest request) throws Exception {

        NodeIdAndOqId msgId = new NodeIdAndOqId(request.getCommandFromId(), request.getOpaque());


        if (request.getRpcCommandKind().equals(HEART_BEAT)) {
            RemotingResponse remotingResponse = node.handleHeart(request);

            remotingResponse.setMsgId(msgId);
            remotingResponse.setSuccess(true);
            return remotingResponse;
        }else if (request.getRpcCommandKind().equals(RequestVote)){
            RemotingResponse remotingResponse = node.handleRequestVote(request);
            remotingResponse.setMsgId(msgId);
            remotingResponse.setSuccess(true);
            System.out.println(remotingResponse);


            return remotingResponse;
        }

        return null;
    }
}
