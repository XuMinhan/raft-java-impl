package node;

import net.RPC.entity.SingleLog;

import java.util.LinkedList;

public class PersistentState {
    private volatile Integer currentTerm;
    private volatile Integer votedFor;
    private LinkedList<SingleLog> logs;

    public PersistentState() {
        this.currentTerm = 0;
        this.votedFor = -1;
        this.logs = new LinkedList<>();
    }

    // Getters and Setters
    public Integer getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(Integer currentTerm) {
        this.currentTerm = currentTerm;
    }

    public Integer getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(Integer votedFor) {
        this.votedFor = votedFor;
    }

    public LinkedList<SingleLog> getLogs() {
        return logs;
    }

    public void setLogs(LinkedList<SingleLog> logs) {
        this.logs = logs;
    }


    public Integer getLastIndexOfLogs(){
        if (logs.isEmpty()) {
            return 0;
        }
        return logs.getLast().getLogId();
    }
    public Integer getLastTermOfLogs(){
        if (logs.isEmpty()) {
            return 1;
        }
        return logs.getLast().getTerm();
    }

}
