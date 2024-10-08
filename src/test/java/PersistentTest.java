import net.RPC.entity.SingleLog;
import node.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;


//      持久化测试
public class PersistentTest {

    private static final int NODE_ID = 2;
    private Node node;

    @BeforeEach
    public void setUp() {
        // 每次测试前初始化节点
        node = new Node(NODE_ID,"nodes.yml");
    }

    @AfterEach
    public void tearDown() {
        // 测试结束后删除持久化文件
        deleteFile("persistent_state_node_" + NODE_ID + ".bin");
        deleteFile("logs_node_" + NODE_ID + ".bin");
    }

    @Test
    public void testUpdateTermAndVotedFor() {
        // 初始状态
        assertEquals(0, node.getPersistentState().getCurrentTerm());
        assertNull(node.getPersistentState().getVotedFor());

        // 更新任期
        node.updateTerm(5);
        assertEquals(5, node.getPersistentState().getCurrentTerm());

        // 更新投票
        node.updateVotedFor(2);
        assertEquals(2, node.getPersistentState().getVotedFor());

        // 重新加载节点，验证持久化
        Node newNode = new Node(NODE_ID,"nodes.yml");
        assertEquals(5, newNode.getPersistentState().getCurrentTerm());
        assertEquals(2, newNode.getPersistentState().getVotedFor());
    }

    @Test
    public void testAddLog() {
        // 初始日志列表应为空
        LinkedList<SingleLog> logs = node.getPersistentState().getLogs();
        assertEquals(0, logs.size());

        // 添加日志
        SingleLog log1 = new SingleLog();
        log1.setLogId(1);
        node.addLog(log1);
        assertEquals(1, node.getPersistentState().getLogs().size());

        // 验证日志内容
        assertEquals(log1, node.getPersistentState().getLogs().getLast());

        // 重新加载节点，验证日志持久化
        Node newNode = new Node(NODE_ID,"nodes.yml");
        assertEquals(1, newNode.getPersistentState().getLogs().size());
        assertEquals(log1.getLogId(), newNode.getPersistentState().getLogs().getLast().getLogId());

        SingleLog log2 = new SingleLog();
        log2.setLogId(2);
        newNode.addLog(log2);

        newNode = new Node(NODE_ID,"nodes.yml");
        assertEquals(log2.getLogId(), newNode.getPersistentState().getLogs().getLast().getLogId());
        assertEquals(2, newNode.getPersistentState().getLogs().getLast().getLogId());
    }

    @Test
    public void testBackLog() {
        // 添加多个日志
        SingleLog log1 = new SingleLog();
        log1.setLogId(1);
        node.addLog(log1);

        SingleLog log2 = new SingleLog();
        log2.setLogId(2);
        node.addLog(log2);

        assertEquals(2, node.getPersistentState().getLogs().size());

        // 回退日志
        node.backLog();
        assertEquals(1, node.getPersistentState().getLogs().size());
        assertEquals(log1, node.getPersistentState().getLogs().getLast());

        // 重新加载节点，验证日志持久化
        Node newNode = new Node(NODE_ID,"nodes.yml");
        assertEquals(1, newNode.getPersistentState().getLogs().size());
        assertEquals(log1.getLogId(), newNode.getPersistentState().getLogs().getLast().getLogId());
    }

    @Test
    public void testTruncateLogsAfter() {
        // 添加多个日志
        SingleLog log1 = new SingleLog();
        log1.setLogId(1);
        node.addLog(log1);

        SingleLog log2 = new SingleLog();
        log2.setLogId(2);
        node.addLog(log2);

        SingleLog log3 = new SingleLog();
        log3.setLogId(3);
        node.addLog(log3);

        assertEquals(3, node.getPersistentState().getLogs().size());

        // 删除ID为2之后的所有日志
        node.truncateLogsAfter(2);

        assertEquals(2, node.getPersistentState().getLogs().size());
        assertEquals(log2.getLogId(), node.getPersistentState().getLogs().getLast().getLogId());

        // 重新加载节点，验证日志持久化
        Node newNode = new Node(NODE_ID,"nodes.yml");
        assertEquals(2, newNode.getPersistentState().getLogs().size());
        assertEquals(log2.getLogId(), newNode.getPersistentState().getLogs().getLast().getLogId());

        // 再次删除ID为1之后的所有日志
        newNode.truncateLogsAfter(1);
        assertEquals(1, newNode.getPersistentState().getLogs().size());
        assertEquals(log1.getLogId(), newNode.getPersistentState().getLogs().getLast().getLogId());
    }

    private void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            assertTrue(file.delete(), "Failed to delete file: " + fileName);
        }
    }
}
