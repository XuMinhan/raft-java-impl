package net.RPC.entity;

public class RemotingResponse {
    private Boolean success;
    private String message;
    private NodeIdAndOqId msgId;

    public NodeIdAndOqId getMsgId() {
        return msgId;
    }

    public void setMsgId(NodeIdAndOqId msgId) {
        this.msgId = msgId;
    }

    public RemotingResponse(NodeIdAndOqId msgId) {
        this.msgId = msgId;
    }

    public RemotingResponse() {
    }


    public RemotingResponse(Boolean success, Integer term,String message) {
        this.success = success;
        this.message = message;
        this.term = term;
    }

    public RemotingResponse(Boolean success, Integer term) {
        this.success = success;
        this.term = term;
    }

    private Integer term;


    @Override
    public String toString() {
        return "RemotingResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", msgId=" + msgId +
                ", term=" + term +
                '}';
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
