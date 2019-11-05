package systems.hedgehog.model.graph.subelement;

import systems.hedgehog.model.struct.OrderInGraph;

import java.util.List;

public class ResultSubgraph {

    private List<OrderInGraph> resultSubgraph;
    private int maxLateness;

    public ResultSubgraph(List<OrderInGraph> resultSubgraph, int maxLateness) {
        this.resultSubgraph = resultSubgraph;
        this.maxLateness = maxLateness;
    }

    public List<OrderInGraph> getResultSubgraph() {
        return resultSubgraph;
    }

    public void setResultSubgraph(List<OrderInGraph> resultSubgraph) {
        this.resultSubgraph = resultSubgraph;
    }

    public int getMaxLateness() {
        return maxLateness;
    }

    public void setMaxLateness(int maxLateness) {
        this.maxLateness = maxLateness;
    }

    @Override
    public String toString() {
        String resultString = "";
        for(OrderInGraph operation : resultSubgraph) {
            resultString += operation.getOrderId() + ",";
        }
        return resultString + " MaxLateness" + maxLateness;
    }
}
