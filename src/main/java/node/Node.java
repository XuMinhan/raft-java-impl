package node;


import net.Processors.RequestFromClientProcessor;
import net.Processors.ResponseCallback;
import net.Processors.RpcRequestProcessor;
import net.Processors.TestRequestProcessor;
import net.RPC.entity.RemotingRequest;
import net.RPC.entity.RemotingResponse;
import net.RPC.entity.SingleLog;
import net.client.NettyClient;
import net.client.RemotingClient;
import net.server.NettyServer;
import net.server.RemotingServer;
import node.utils.PersistenceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.RPC.entity.RPCKind.*;
import static node.Common.*;

public class Node {


    private static final Logger nodeStateLogger = LogManager.getLogger("nodeState");

    public Address getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(Address nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    private Address nodeInfo;
    private int leaderId;
    private ClusterConfig clusterConfig;
    private ConcurrentHashMap<Integer, Address> otherAddress = new ConcurrentHashMap<>();


    // 持久化内容（包含日志）
    private PersistentState persistentState;

    // 非持久化内容
    private Integer commitIndex;
    private Integer lastApplied;

    // Leader 额外非持久化内容
    private HashMap<Integer, Integer> nextIndex;
    private HashMap<Integer, Integer> matchIndex;

    public PersistentState getPersistentState() {
        return persistentState;
    }

    public void setPersistentState(PersistentState persistentState) {
        this.persistentState = persistentState;
    }


    public Integer getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(Integer commitIndex) {
        this.commitIndex = commitIndex;
    }

    public Node(int nodeId, String nodesYml) {
        leaderId = 1;


        // 读取集群配置信息
        InputStream inputStream = Node.class.getClassLoader().getResourceAsStream(nodesYml);
        Yaml yaml = new Yaml();
        this.clusterConfig = yaml.loadAs(inputStream, ClusterConfig.class);
        for (Address node : clusterConfig.getNodes()) {
            if (node.getId() == nodeId) {
                nodeInfo = node;
            } else {
                otherAddress.put(node.getId(), node);
            }
        }

        // 加载持久化内容
        this.persistentState = PersistenceUtils.loadPersistentState(nodeId);
        this.persistentState.setLogs(PersistenceUtils.loadLogs(nodeId));

        // 初始化非持久化内容
        this.commitIndex = 0;
        this.lastApplied = 0;
        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();


        // nodeId 为 1 默认 为Leader
        if (nodeId == 1) {
            nodeState = LEADER_STATE;
            // 作为第一个任期
            updateTerm(1);
        } else nodeState = FOLLOW_STATE;


    }


    public void run() {
        startClient();
        startServer();
        nodeStateLogger.info("节点" + nodeInfo.getId() + "启动成功");
        electionTimer = Executors.newScheduledThreadPool(1);
        heartbeatTimer = Executors.newScheduledThreadPool(1);
        if (nodeState.equals(LEADER_STATE)) {
            // 开始心跳
            becomeLeader();
        }
    }

    public void shutDown() {
        nettyClient.shutdown();
        nettyServer.shutdown();
        if (electionTimer != null && !electionTimer.isShutdown()) {
            electionTimer.shutdownNow();
        }
        if (heartbeatTimer != null && !heartbeatTimer.isShutdown()) {
            heartbeatTimer.shutdownNow();
        }
        System.out.println("节点" + nodeInfo.getId() + "关闭成功");
    }

//******************************************************************
//                          持久化部分
//******************************************************************

    // 更新任期并立即持久化
    public void updateTerm(Integer newTerm) {
        if (!Objects.equals(newTerm, persistentState.getCurrentTerm())) {
            voteLogger.info(nodeInfo.getId()+"持久化任期为"+newTerm);
            persistentState.setCurrentTerm(newTerm);
            savePersistentState(); // 每次更新立即保存
        }
    }

    // 更新投票并立即持久化
    public void updateVotedFor(Integer votedFor) {
        voteLogger.info(nodeInfo.getId()+"持久化投票为"+votedFor);
        if (!Objects.equals(votedFor, persistentState.getVotedFor())) {
            persistentState.setVotedFor(votedFor);
            savePersistentState(); // 每次更新立即保存
        }
    }

    // 保存持久化内容（不包括日志）
    private void savePersistentState() {
        PersistenceUtils.savePersistentState(nodeInfo.getId(), persistentState);
    }

    // 添加日志并立即持久化（顺序写入）
    public void addLog(SingleLog log) {
        persistentState.getLogs().add(log);
        PersistenceUtils.addLogEntry(nodeInfo.getId(), log);
    }

    // 回退日志并立即持久化（顺序删除最后一条）
    public void backLog() {
        LinkedList<SingleLog> logs = persistentState.getLogs();
        if (!logs.isEmpty()) {
            logs.removeLast();
            PersistenceUtils.backLogEntry(nodeInfo.getId());
        } else {
            System.out.println("No logs to backtrack.");
        }
    }

    public void truncateLogsAfter(int logId) {
        // 获取当前的日志列表
        LinkedList<SingleLog> logs = persistentState.getLogs();

        // 找到需要保留的日志条目位置，删除其后的所有条目
        int index = -1;
        for (int i = logs.size() - 1; i >= 0; i--) {
            if (logs.get(i).getLogId() == logId) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            System.out.println("Log with ID " + logId + " not found. No logs truncated.");
            return;
        }

        // 从ID为logId的位置之后截断日志
        logs.subList(index + 1, logs.size()).clear();

        // 更新持久化状态的日志列表
        persistentState.setLogs(logs);

        // 保存更新后的日志到硬盘
        PersistenceUtils.saveLogs(nodeInfo.getId(), logs);
    }

    //*****************************************************************
    //                         网络部分
    //*****************************************************************
    private NettyServer nettyServer;
    private RemotingServer remotingServer;

    private void startServer() {
        remotingServer = new RemotingServer();
        remotingServer.setNode(this);
        remotingServer.registerProcessor(COMMAND_FROM_CLIENT, new RequestFromClientProcessor(this));
        remotingServer.registerProcessor(COMMAND_FROM_RPC, new RpcRequestProcessor(this));
        remotingServer.registerProcessor(COMMAND_TEST, new TestRequestProcessor(this));
        int port = nodeInfo.getPort();
        nettyServer = new NettyServer(port, remotingServer);
        new Thread(() -> {
            try {
                nettyServer.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private RemotingClient remotingClient;
    private NettyClient nettyClient;

    private void startClient() {
        remotingClient = new RemotingClient();
        remotingClient.setNode(this);
        remotingClient.setOtherAddress(otherAddress);
        nettyClient = new NettyClient(remotingClient);
        nettyClient.start();
    }

    public RemotingResponse syncSendRequest(int nodeId, RemotingRequest request, ResponseCallback callback) throws InterruptedException {
        return remotingClient.send(nodeId, request, callback).getResponse();
    }

    public RemotingResponse asyncSendRequest(int nodeId, RemotingRequest request, ResponseCallback callback) throws InterruptedException {
        remotingClient.asyncSend(nodeId, request, callback);
        return null;

    }


    //****************************************************
    //                  节点心跳部分
    //****************************************************
    private Integer nodeState; // Follower, Candidate, Leader
    // 选举超时与心跳定时器
    private ScheduledExecutorService electionTimer;
    private ScheduledExecutorService heartbeatTimer;
    // 常量定义选举超时的最小和最大值（以毫秒为单位）
    private static final int ELECTION_TIMEOUT_MIN = 300; // 最小选举超时
    private static final int ELECTION_TIMEOUT_MAX = 500; // 最大选举超时

    public void startHeartbeat() {
        heartbeatLogger.info("节点" + nodeInfo.getId() + "发送心跳请求");

        heartbeatTimer.scheduleAtFixedRate(() -> {
            if (Objects.equals(nodeState, LEADER_STATE)) {

                for (Address address : otherAddress.values()) {
                    // 发送心跳请求
                    try {
                        // 构建心跳消息
                        RemotingRequest heartbeatRequest = new RemotingRequest();
                        heartbeatRequest.setKind(COMMAND_FROM_RPC);
                        heartbeatRequest.setRpcCommandKind(HEART_BEAT);
                        heartbeatRequest.setCommandFromId(nodeInfo.getId());
                        heartbeatRequest.setTerm(persistentState.getCurrentTerm());
                        heartbeatRequest.setLeaderId(nodeInfo.getId());
                        heartbeatRequest.setPrevLogIndex(persistentState.getLastIndexOfLogs());
                        heartbeatRequest.setPrevLogTerm(persistentState.getLastTermOfLogs());
                        heartbeatRequest.setEntries(null); // 空的日志条目表示心跳
                        heartbeatRequest.setLeaderCommit(commitIndex);
                        sendHeartbeat(address, heartbeatRequest);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, 0, 50, TimeUnit.MILLISECONDS); // 每 50 毫秒发送一次心跳
    }

    private static final Logger heartbeatLogger = LogManager.getLogger("heartbeat");

    // 发送心跳请求
    private void sendHeartbeat(Address address, RemotingRequest heartbeatRequest) throws InterruptedException {
        asyncSendRequest(address.getId(), heartbeatRequest, response -> {
            if (!response.getSuccess()) {
//                System.out.println(heartbeatRequest);
                heartbeatLogger.info("心跳请求异常");
                return;
            }
//            System.out.println(heartbeatRequest);
            if (response.getTerm() > persistentState.getCurrentTerm()) {
                heartbeatLogger.debug(nodeInfo.getId() + "心跳返回任期大于本节点任期，状态下放为follower");
//                becomeFollower(response.getTerm(), null);
            }
            //todo 日志复制
        });
    }

    //节点可能是任何一个状态
    //todo log的处理
    /*
        如果任期小，说明 leader 分区后，重新选举，原leader重新连接上了，发送拒绝请求并且通知最新的任期，发送者需要降级
        如果任期大，说明发生重新选举，不管本节点是什么状态，设置为follower状态即可，更新任期，重置定时器
        如果任期一样，说明一切正常，重置定时器
     */

    public RemotingResponse handleHeart(RemotingRequest request) {

        // 如果请求中的任期小于当前任期，拒绝请求
        if (request.getTerm() < persistentState.getCurrentTerm()) {
            heartbeatLogger.debug(nodeInfo.getId() + "心跳中任期小于节点任期，拒绝");
            return new RemotingResponse(false, persistentState.getCurrentTerm());
        }

        // 如果请求中的任期比当前任期大，更新当前任期并转换为 Follower
        if (request.getTerm() > persistentState.getCurrentTerm()) {
            heartbeatLogger.debug(nodeInfo.getId() + "心跳中任期大于节点任期，更新任期");
            becomeFollower(request.getTerm(), request.getLeaderId());
        }

        // 更新领导者 ID
        leaderId = request.getLeaderId();
        // 重置选举超时，防止自己成为 Candidate
        heartbeatLogger.info(nodeInfo.getId() + "重置选举定时器");
        resetElectionTimeout();

        // 更新已提交日志索引
        if (request.getLeaderCommit() > commitIndex) {
            commitIndex = Math.min(request.getLeaderCommit(), persistentState.getLastIndexOfLogs());
        }

        // 返回成功的响应
        return new RemotingResponse(true, persistentState.getCurrentTerm());
    }

    public void becomeFollower(int term, Integer leaderId) {
        // 重置选举超时
        voteLogger.info("节点"+nodeInfo.getId()+"重置心跳计时器");
        resetElectionTimeout();

        this.nodeState = FOLLOW_STATE;  // 将当前节点状态设置为 Follower
        nodeStateLogger.info("节点" + nodeInfo.getId() + "成为Follower");
        voteLogger.info("节点" + nodeInfo.getId() + "更新任期为" + term);

        if (term> persistentState.getCurrentTerm()) {
            this.updateTerm(term);  // 更新任期
            this.updateVotedFor(-1);  // 清空投票对象，因为新的任期会重新投票
        }
        this.leaderId = (leaderId != null) ? leaderId : -1;  // 更新领导者 ID，如果未知则设置为 -1

    }

    // 获取一个随机的选举超时时间，介于 ELECTION_TIMEOUT_MIN 和 ELECTION_TIMEOUT_MAX 之间
    private int getRandomElectionTimeout() {
        return ThreadLocalRandom.current().nextInt(ELECTION_TIMEOUT_MIN, ELECTION_TIMEOUT_MAX + 1);
    }

    public void resetElectionTimeout() {
        heartbeatLogger.info("开始选举计时器");
        // 如果之前的选举定时任务正在执行，则取消它
        if (electionTimer != null && !electionTimer.isShutdown()) {
            electionTimer.shutdownNow();
        }


        // 使用一个单线程调度执行器来设置选举超时
        electionTimer = Executors.newScheduledThreadPool(1);
            int electionTimeout = getRandomElectionTimeout();


            // 在延迟后再开始选举超时计时器
            electionTimer.schedule(() -> {
                if (Objects.equals(nodeState, FOLLOW_STATE)) {
                    voteLogger.info(nodeInfo.getId() + "成为候选人");
                    becomeCandidate(); // 如果当前仍是 Follower 并且超时，转换为 Candidate 发起选举
                }
            }, electionTimeout, TimeUnit.MILLISECONDS);


    }


    //****************************************************
    //                  节点选举部分
    //****************************************************

    private static final Logger voteLogger = LogManager.getLogger("vote");

    public void becomeCandidate() {
        nodeStateLogger.info("节点" + nodeInfo.getId() + "成为candidate" + "将任期从"
                + persistentState.getCurrentTerm() + "+1");

        // 状态转换为 Candidate
        this.nodeState = CANDIDATE_STATE;

        // 自增当前任期
        updateTerm(persistentState.getCurrentTerm() + 1);

        // 给自己投票
        updateVotedFor(nodeInfo.getId());
        AtomicInteger votesGranted = new AtomicInteger(1); // 自己的票

        // 重置选举超时
        resetElectionTimeout();


        // 向其他节点发送 RequestVote 请求
        for (Address address : otherAddress.values()) {
            // 构建 RequestVote 请求
            RemotingRequest voteRequest = new RemotingRequest();
            voteRequest.setKind(COMMAND_FROM_RPC);
            voteRequest.setRpcCommandKind(RequestVote);
            voteRequest.setTerm(persistentState.getCurrentTerm());
            voteRequest.setCommandFromId(nodeInfo.getId());
            voteRequest.setPrevLogIndex(persistentState.getLastIndexOfLogs());
            voteRequest.setPrevLogTerm(persistentState.getLastTermOfLogs());
            // 伪代码：通过网络发送 RequestVote 请求，并处理响应
            sendRequestVote(address, voteRequest, (response) -> {
                voteLogger.info(nodeInfo.getId() + "收到" + address.getId() + "的投票结果");
                handleRequestVoteResponse(response, votesGranted);
            });
        }
    }

    // 发送 RequestVote 请求
    private void sendRequestVote(Address address, RemotingRequest request, ResponseCallback callback) {
        try {
            asyncSendRequest(address.getId(), request, callback);
        } catch (InterruptedException e) {
            heartbeatLogger.info(address.getId() + "号节点掉线");
        }
        // 使用网络组件将 RequestVote 请求发送给目标地址
        // 在收到响应后调用 callback
    }

    // 处理 RequestVote 响应
    public void handleRequestVoteResponse(RemotingResponse response, AtomicInteger votesGranted) {
        if (!response.getSuccess()) {
            return;
        }
        if (response.getTerm() > persistentState.getCurrentTerm()) {
            // 响应中的任期比当前任期大，更新任期并转换为 Follower
//            updateTerm(response.getTerm());
            becomeFollower(persistentState.getCurrentTerm(), null);
            return;
        }

        if (response.getMessage().equals("yeSir")) {

            votesGranted.addAndGet(1);
            voteLogger.info("节点" + nodeInfo.getId() + "获得了" + votesGranted + "个投票");
            // 检查是否获得大多数投票
            if (votesGranted.get() > (otherAddress.size() / 2)) {
                // 成为 Leader
                voteLogger.info(nodeInfo.getId() + "成为 Leader");
                becomeLeader();
            }
        }
    }

    public void becomeLeader() {

        // 将当前节点状态设置为 Leader
        this.nodeState = LEADER_STATE;
        nodeStateLogger.info("节点" + nodeInfo.getId() + "成为Leader");

        // 清空当前的 votedFor，因为成为 Leader 后不再需要投票
        updateVotedFor(-1);
        // 初始化 Leader 的非持久化内容
        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();

        otherAddress.forEach(
                (index, address) -> {
                    int id = address.getId();
                    nextIndex.put(id, persistentState.getLogs().size());
                    matchIndex.put(id, 0);  // matchIndex 初始为 0，表示还没有成功复制日志
                }
        );
        // 启动心跳定时器，定期向 Follower 发送心跳消息
        startHeartbeat();
    }


    public synchronized RemotingResponse handleRequestVote(RemotingRequest request) {
        voteLogger.info("节点" + nodeInfo.getId() + "处理来自节点" + request.getCommandFromId() + "的投票请求");
        // 如果请求的任期小于当前任期，拒绝投票
        if (request.getTerm() < persistentState.getCurrentTerm()) {
            voteLogger.info("节点" + nodeInfo + "拒绝为节点" + request.getCommandFromId() + "投票");

            return new RemotingResponse(true, persistentState.getCurrentTerm(), "noSir");
        }

        // 如果请求的任期比当前任期大，更新当前任期并转换为 Follower
        if (request.getTerm() > persistentState.getCurrentTerm()) {
            voteLogger.info("节点" + nodeInfo.getId() + "接收到更大的任期，更新任期到" + request.getTerm());
            becomeFollower(request.getTerm(), null);
        }

        // 检查是否已经投过票或者请求者的日志是否足够新
        boolean logIsUpToDate = isLogUpToDate(request.getPrevLogTerm(), request.getPrevLogIndex());
        if ((persistentState.getVotedFor() == -1) && logIsUpToDate) {

            voteLogger.info("节点" + nodeInfo + "为节点" + request.getCommandFromId() +
                    "投票,此时任期为"+persistentState.getCurrentTerm()+
                    "此时已经给节点"+persistentState.getVotedFor()+"投过票");

            // 更新投票记录并返回投票
            updateTerm(request.getTerm());
            updateVotedFor(request.getCommandFromId());


            RemotingResponse remotingResponse = new RemotingResponse(true, persistentState.getCurrentTerm());
            remotingResponse.setMessage("yeSir");
            becomeFollower(request.getTerm(), null);


            return remotingResponse;
        }

        // 如果条件不满足，拒绝投票
        RemotingResponse remotingResponse = new RemotingResponse(true, persistentState.getCurrentTerm());
        remotingResponse.setMessage("noSir");
        voteLogger.info("节点" + nodeInfo + "拒绝为节点" + request.getCommandFromId() + "投票");
        return remotingResponse;
    }

    // 判断请求者的日志是否比当前节点的日志更新
    private boolean isLogUpToDate(int requestLastLogTerm, int requestLastLogIndex) {
        int lastLogTerm = persistentState.getLogs().isEmpty() ? 0 : persistentState.getLogs().getLast().getTerm();
        int lastLogIndex = persistentState.getLogs().size() - 1;

        // 如果请求者的最后日志的任期号比当前节点的更大，表示日志更新
        if (requestLastLogTerm > lastLogTerm) {
            return true;
        }
        // 如果请求者的最后日志的任期号相同，并且索引较大，也表示日志更新
        if (requestLastLogTerm == lastLogTerm && requestLastLogIndex >= lastLogIndex) {
            return true;
        }
        return false;
    }


}
