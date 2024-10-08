package node.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import net.RPC.entity.RemotingRequest;
import net.RPC.entity.RemotingResponse;
import net.RPC.entity.SingleLog;
import node.PersistentState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.LinkedList;


public class PersistenceUtils {
    private static final Logger persisLogger = LogManager.getLogger("persistence");

    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(PersistentState.class);
        kryo.register(LinkedList.class);
        kryo.register(SingleLog.class);
        return kryo;
    });


    // 加载持久化状态（不包含日志）
    public static PersistentState loadPersistentState(int nodeId) {
        Kryo kryo = kryoThreadLocal.get();
        String fileName = "persistent_state_node_" + nodeId + ".bin";
        File file = new File(fileName);

        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                persisLogger.debug("Loaded persistent state for Node " + nodeId);
                return kryo.readObject(input, PersistentState.class);
            } catch (Exception e) {
                e.printStackTrace();
                persisLogger.debug("Failed to load persistent state. Initializing with default values.");
            }
        } else {
            persisLogger.debug("Persistent state file not found. Creating new default state.");
            PersistentState defaultState = new PersistentState();
            savePersistentState(nodeId, defaultState);
            return defaultState;
        }

        return new PersistentState();
    }

    // 保存持久化状态（不包含日志）
    public static void savePersistentState(int nodeId, PersistentState persistentState) {
        Kryo kryo = kryoThreadLocal.get();

        String fileName = "persistent_state_node_" + nodeId + ".bin";
        try (Output output = new Output(new FileOutputStream(fileName))) {
            kryo.writeObject(output, persistentState);
            persisLogger.debug("Saved persistent state for Node " + nodeId);
        } catch (Exception e) {
            e.printStackTrace();
            persisLogger.debug("Failed to save persistent state.");
        }
    }

    // 保存日志到文件
// 保存日志到文件
    public static void saveLogs(int nodeId, LinkedList<SingleLog> logs) {
        String fileName = "logs_node_" + nodeId + ".bin";
        try (FileOutputStream fos = new FileOutputStream(fileName);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            for (SingleLog log : logs) {
                oos.writeObject(log);
            }
            oos.flush();
            persisLogger.debug("Logs saved for Node " + nodeId);
        } catch (IOException e) {
            e.printStackTrace();
            persisLogger.debug("Failed to save logs.");
        }
    }


    // 追加保存日志条目
    public static void addLogEntry(int nodeId, SingleLog log) {
        String fileName = "logs_node_" + nodeId + ".bin";
        File file = new File(fileName);

        try (FileOutputStream fos = new FileOutputStream(file, true);
             ObjectOutputStream oos = file.exists() && file.length() > 0 ? new AppendedObjectOutputStream(fos) : new ObjectOutputStream(fos)) {
            oos.writeObject(log);
            oos.flush();
            persisLogger.debug("Log entry added for Node " + nodeId);
        } catch (IOException e) {
            e.printStackTrace();
            persisLogger.debug("Failed to add log entry.");
        }
    }

    // 读取所有日志条目
    public static LinkedList<SingleLog> loadLogs(int nodeId) {
        LinkedList<SingleLog> logs = new LinkedList<>();
        String fileName = "logs_node_" + nodeId + ".bin";

        File file = new File(fileName);
        if (!file.exists()) {
            persisLogger.debug("Log file not found for Node " + nodeId + ". Creating new empty log file.");
            try {
                if (file.createNewFile()) {
                    persisLogger.debug("Log file created for Node " + nodeId);
                }
            } catch (IOException e) {
                e.printStackTrace();
                persisLogger.debug("Failed to create log file.");
            }
            return logs; // Return empty log list
        }

        // 如果文件大小为零，直接返回空的日志列表
        if (file.length() == 0) {
            persisLogger.debug("Log file for Node " + nodeId + " is empty. Returning empty log list.");
            return logs;
        }

        // 读取日志文件
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            while (true) {
                try {
                    SingleLog log = (SingleLog) ois.readObject();
                    logs.add(log);
                } catch (EOFException e) {
                    break; // End of file reached
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            persisLogger.debug("Failed to load logs.");
        }

        return logs;
    }

    // 回退最后一个日志条目
    public static void backLogEntry(int nodeId) {
        String fileName = "logs_node_" + nodeId + ".bin";
        LinkedList<SingleLog> logs = loadLogs(nodeId);
        if (!logs.isEmpty()) {
            logs.removeLast();
            try (FileOutputStream fos = new FileOutputStream(fileName);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                for (SingleLog log : logs) {
                    oos.writeObject(log);
                }
                oos.flush();
                persisLogger.debug("Last log entry removed for Node " + nodeId);
            } catch (IOException e) {
                e.printStackTrace();
                persisLogger.debug("Failed to backtrack log entry.");
            }
        } else {
            persisLogger.debug("No logs to backtrack for Node " + nodeId);
        }
    }
}
