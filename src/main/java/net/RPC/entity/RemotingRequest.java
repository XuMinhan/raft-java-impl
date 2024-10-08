package net.RPC.entity;

import java.util.LinkedList;
import java.util.List;

public class RemotingRequest {

    /*
    （AppendEntries）RPC
        term            领导人的任期，如果收到的任期号小于自己的任期号，跟随者会拒绝请求，从而防止过时的领导者影响系统的状态
        leaderId        领导人 ID 因此跟随者可以对客户端进行重定向
        prevLogIndex    紧邻新日志条目之前的日志条目的索引
        prevLogTerm     上个日志条目的任期
        entries[]
        leaderCommit    leaderCommit是领导人已知的已提交的最高日志条目的索引。用于更新
     */
    Integer opaque; // 命令id
    /*
        发送日志通信
        节点之间通信
        测试
     */
    Integer kind;
    Integer commandFromId;

    public Integer getCommandFromId() {
        return commandFromId;
    }

    @Override
    public String toString() {
        return "RemotingRequest{" +
                "opaque=" + opaque +
                ", kind=" + kind +
                ", commandFromId=" + commandFromId +
                ", rpcCommandKind=" + rpcCommandKind +
                ", term=" + term +
                ", leaderId=" + leaderId +
                ", prevLogIndex=" + prevLogIndex +
                ", prevLogTerm=" + prevLogTerm +
                ", entries=" + entries +
                ", leaderCommit=" + leaderCommit +
                '}';
    }

    public void setCommandFromId(Integer commandFromId) {
        this.commandFromId = commandFromId;
    }

    /*

     */
    Integer rpcCommandKind;
    Integer term;

    public Integer getRpcCommandKind() {
        return rpcCommandKind;
    }

    public void setRpcCommandKind(Integer rpcCommandKind) {
        this.rpcCommandKind = rpcCommandKind;
    }

    Integer leaderId;
    Integer prevLogIndex;
    Integer prevLogTerm;
    List<SingleLog> entries;


    Integer leaderCommit;

    public Integer getOpaque() {
        return opaque;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Integer leaderId) {
        this.leaderId = leaderId;
    }

    public Integer getPrevLogIndex() {
        return prevLogIndex;
    }

    public void setPrevLogIndex(Integer prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public Integer getPrevLogTerm() {
        return prevLogTerm;
    }

    public void setPrevLogTerm(Integer prevLogTerm) {
        this.prevLogTerm = prevLogTerm;
    }

    public List<SingleLog> getEntries() {
        return entries;
    }

    public void setEntries(List<SingleLog> entries) {
        this.entries = entries;
    }

    public Integer getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(Integer leaderCommit) {
        this.leaderCommit = leaderCommit;
    }

    public void setOpaque(Integer opaque) {
        this.opaque = opaque;
    }


    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

}
