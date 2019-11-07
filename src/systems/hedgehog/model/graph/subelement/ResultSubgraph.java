package systems.hedgehog.model.graph.subelement;

import systems.hedgehog.model.struct.OrderInGraph;

import java.util.List;

public class ResultSubgraph {

    private List<OrderInGraph> resultSubgraph;
    private int maxLateness;
    private String machine;

    public ResultSubgraph(List<OrderInGraph> resultSubgraph, int maxLateness, String machine) {
        this.resultSubgraph = resultSubgraph;
        this.maxLateness = maxLateness;
        this.machine = machine;
    }

    public List<OrderInGraph> getResultSubgraph() {
        return resultSubgraph;
    }

    public int getMaxLateness() {
        return maxLateness;
    }

    public String getMachine() {
        return machine;
    }

    @Override
    public String toString() {
        String resultString = "Machine " + machine + ", Result ";
        for(OrderInGraph operation : resultSubgraph) {
            resultString += operation.getOrderId() + ",";
        }
        return resultString + " MaxLateness: " + maxLateness;
    }
}
