package systems.hedgehog.model.graph.subelement;

import systems.hedgehog.model.struct.OrderInGraph;

import java.util.List;

public class ResultSubgraph {

    private List<Edge> resultSubgraph;
    private int maxLateness;
    private String machine;

    public ResultSubgraph(List<Edge> resultSubgraph, int maxLateness, String machine) {
        this.resultSubgraph = resultSubgraph;
        this.maxLateness = maxLateness;
        this.machine = machine;
    }

    public List<Edge> getResultSubgraph() {
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
        for(Edge edge : resultSubgraph) {
            resultString += edge.toString() + ",";
        }
        return resultString + " MaxLateness: " + maxLateness;
    }
}
