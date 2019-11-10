package systems.hedgehog.model.graph.subelement;

import java.util.List;

public class ResultSubgraph {

    private final List<Edge> resultSubgraph;
    private final int maxLateness;
    private final String machine;

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
        StringBuilder resultString = new StringBuilder("Machine: " + machine + ", Result: ");
        for(Edge edge : resultSubgraph) {
            resultString.append(edge.toString()).append(",");
        }
        return resultString + " Max Lateness: " + maxLateness;
    }
}
