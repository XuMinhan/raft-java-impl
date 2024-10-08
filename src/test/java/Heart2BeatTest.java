import net.RPC.entity.RemotingRequest;
import net.RPC.entity.RemotingResponse;
import node.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;

import static net.RPC.entity.RPCKind.COMMAND_TEST;
import static net.RPC.entity.RPCKind.HEART_BEAT;
import static node.utils.FileOperation.deleteFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Heart2BeatTest {
    Node node1;
    Node node2;

    LinkedList<Node> nodes = new LinkedList<>();
    @BeforeEach
    public void  setup() {
        node1 = new Node(1,"2nodesTest.yml");
        node2 = new Node(2,"2nodesTest.yml");


        nodes.add(node1);
        nodes.add(node2);

        node1.run();
        node2.run();

    }

    @AfterEach
    public void tearDown(){
        node1.shutDown();
        node2.shutDown();
        for (Node node : nodes) {
            deleteFile("persistent_state_node_" + node.getNodeInfo().getId() + ".bin");
            deleteFile("logs_node_" + node.getNodeInfo().getId() + ".bin");

        }
    }
    @Test
    public void heartBeatForaWhile() throws InterruptedException {
        Thread.sleep(1000*10);
    }
}
