package net.Processors;

import net.RPC.entity.RemotingResponse;

public interface ResponseCallback {
    void onResponse(RemotingResponse response);
}
