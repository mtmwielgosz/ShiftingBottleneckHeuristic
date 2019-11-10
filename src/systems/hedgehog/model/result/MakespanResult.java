package systems.hedgehog.model.result;

import systems.hedgehog.model.graph.subelement.Edge;

import java.util.List;

public class MakespanResult {

    public MakespanResult(int makespan, List<Edge> edges) {
        this.makespan = makespan;
        this.edges = edges;
    }

    public int getMakespan() {
        return makespan;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    private int makespan;
    private List<Edge> edges;
}
