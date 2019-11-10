package systems.hedgehog.model.graph.calc;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.sub.Edge;
import systems.hedgehog.model.graph.sub.Order;

import java.util.Optional;

public class DueDateCalc {

    private final Graph graph;

    public DueDateCalc(Graph graph) {
        this.graph = graph;
    }

    public int getDueDateFor(String currentMachine, int endingOrderId) {
        return graph.getMakespan() - graph.getMakespanForEndingOrder(endingOrderId).getMakespan()
                + graph.getProductionTimeIncludingBlockingFor(currentMachine, endingOrderId) + graph.getReleaseTimeIncludingBlockingFor(currentMachine, endingOrderId);
    }

    public int getDueDateForEdge(Edge edge) {
        Optional<Order> orderOfEdge = graph.getOrder(edge);
        if(orderOfEdge.isPresent()) {
            return graph.getMakespan() - graph.getMakespanForEndingOrder(orderOfEdge.get().getOrderId()).getMakespan()
                    + edge.getWeight() + graph.getReleaseTimeForEdge(edge);
        }
        throw new IllegalArgumentException("Edge " + edge + " is not included in any order.");
    }
}
