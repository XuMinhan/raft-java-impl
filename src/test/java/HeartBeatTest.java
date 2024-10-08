import node.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static node.utils.FileOperation.deleteFile;

public class HeartBeatTest {
    Node node1;
    Node node2;
    Node node3;
    Node node4;
    Node node5;
    LinkedList<Node> nodes = new LinkedList<>();
    @BeforeEach
    public void  setup() {
        node1 = new Node(1,"5nodesTest.yml");
        node2 = new Node(2,"5nodesTest.yml");
        node3 = new Node(3,"5nodesTest.yml");
        node4 = new Node(4,"5nodesTest.yml");
        node5 = new Node(5,"5nodesTest.yml");

        nodes.add(node1);
        nodes.add(node2);
        nodes.add(node3);
        nodes.add(node4);
        nodes.add(node5);
        node1.run();
        node2.run();
        node3.run();
        node4.run();
        node5.run();
    }

    @AfterEach
    public void tearDown(){
        node1.shutDown();
        node2.shutDown();
        node3.shutDown();
        node4.shutDown();
        node5.shutDown();
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
