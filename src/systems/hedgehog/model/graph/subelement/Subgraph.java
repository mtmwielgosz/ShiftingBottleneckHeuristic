package systems.hedgehog.model.graph.subelement;

import java.util.List;

public class Subgraph {

    private int orderId;
    private List<Edge> edges;

    private int maxMakespan;

    public int getOrderId() {
        return orderId;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public int getMaxMakespan() {
        return maxMakespan;
    }

    public Subgraph(int orderId, List<Edge> edges, int maxMakespan) {
        this.orderId = orderId;
        this.edges = edges;
        this.maxMakespan = maxMakespan;
    }
}
