package node;

import java.util.List;

public class ClusterConfig {
    private List<Address> nodes;

    // Getter and Setter
    public List<Address> getNodes() {
        return nodes;
    }

    public void setNodes(List<Address> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "ClusterConfig{" +
                "nodes=" + nodes +
                '}';
    }
}

