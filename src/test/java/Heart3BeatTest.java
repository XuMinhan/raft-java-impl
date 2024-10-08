import node.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static node.utils.FileOperation.deleteFile;

public class Heart3BeatTest {
    Node node1;
    Node node2;
    Node node3;

    LinkedList<Node> nodes = new LinkedList<>();
    @BeforeEach
    public void  setup() {
        node1 = new Node(1,"3nodesTest.yml");
        node2 = new Node(2,"3nodesTest.yml");
        node3 = new Node(3,"3nodesTest.yml");


        nodes.add(node1);
        nodes.add(node2);
        nodes.add(node3);

        node1.run();
        node2.run();
        node3.run();
    }

    @AfterEach
    public void tearDown(){
        node1.shutDown();
        node2.shutDown();
        node3.shutDown();
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
