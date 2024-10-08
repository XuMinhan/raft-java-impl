package net.RPC.entity;

import java.io.Serializable;

public class SingleLog implements Serializable {
    Integer logId;
    Integer term;
    String content;

    public Integer getLogId() {
        return logId;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    @Override
    public String toString() {
        return "SingleLog{" +
                "logId=" + logId +
                ", term=" + term +
                ", content='" + content + '\'' +
                '}';
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
