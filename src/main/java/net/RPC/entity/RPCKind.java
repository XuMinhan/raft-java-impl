package net.RPC.entity;

import java.util.HashMap;

public class RPCKind {
    public final  static  Integer COMMAND_FROM_CLIENT = 0;
    public final  static  Integer COMMAND_FROM_RPC = 1;
    public final  static  Integer COMMAND_TEST = 200;

    public final static Integer RequestVote = 0;
    public final static Integer AppendEntries = 1;
    public final  static  Integer HEART_BEAT = 2;

    public final static Integer InstallSnapshot = 3;
    public static HashMap<Integer, String> rpcKindMap = new HashMap<>();

    static {
        rpcKindMap.put(RequestVote, "请求投票");
        rpcKindMap.put(AppendEntries, "增加日志");
        rpcKindMap.put(HEART_BEAT, "心跳");
        rpcKindMap.put(InstallSnapshot, "安装快照");
    }

}
