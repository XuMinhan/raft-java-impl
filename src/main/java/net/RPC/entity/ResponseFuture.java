package net.RPC.entity;


import net.Processors.ResponseCallback;

import java.util.concurrent.CountDownLatch;

public class ResponseFuture {
    private final Integer opaque;
    private Boolean success;
    private ResponseCallback callback;

    public RemotingResponse getResponse() {
        return response;
    }

    public ResponseCallback getCallback() {
        return callback;
    }

    public void setCallback(ResponseCallback callback) {
        this.callback = callback;
    }

    public void setResponse(RemotingResponse response) {
        this.response = response;
    }

    private volatile RemotingRequest responseCommand;
    private RemotingResponse response;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public ResponseFuture(Integer opaque,RemotingRequest request) {
        this.responseCommand = request;
        this.opaque = opaque;
    }

    public Integer getOpaque() {
        return opaque;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public RemotingRequest getResponseCommand() {
        return responseCommand;
    }

    public void setResponseCommand(RemotingRequest responseCommand) {
        this.responseCommand = responseCommand;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }
}
