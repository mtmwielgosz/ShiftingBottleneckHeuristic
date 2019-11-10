package systems.hedgehog.model.graph.calc;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.sub.Edge;

import java.util.Optional;
import java.util.Set;

public class ProductionTimeCalc {

    private final Graph graph;

    public ProductionTimeCalc(Graph graph) {
        this.graph = graph;
    }

    public int getProductionTimeFor(String currentMachine, int orderId) {
        Optional<Edge> lastEdge = graph.getLastEdgeFor(currentMachine, orderId);
        if(lastEdge.isPresent()) {
            return lastEdge.get().getWeight();
        }
        return 0;
    }

    public int getProductionTimeIncludingBlockingFor(String currentMachine, int orderId) {
        int maxProductionTime =  getProductionTimeFor(currentMachine, orderId);
        int releaseTimeCurrent =  graph.getReleaseTimeIncludingBlockingFor(currentMachine, orderId);

        Optional<Edge> currentEdge =  graph.getLastEdgeFor(currentMachine, orderId);
        if(currentEdge.isPresent()) {
            Set<Edge> nextEdges = graph.getNextEdgesFor(currentEdge.get());
            for(Edge nextEdge : nextEdges) {
                int releaseTimeForNextEdge = graph.getReleaseTimeIncludingBlockingFor(nextEdge.getFirstNode().getMachine(), orderId);
                if(releaseTimeForNextEdge > releaseTimeCurrent + maxProductionTime) {
                    maxProductionTime = releaseTimeForNextEdge - releaseTimeCurrent;
                }
            }
        }
        return maxProductionTime;
    }
}
