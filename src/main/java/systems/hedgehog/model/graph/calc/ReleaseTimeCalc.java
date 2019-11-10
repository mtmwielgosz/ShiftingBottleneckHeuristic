package systems.hedgehog.model.graph.calc;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.sub.Edge;
import systems.hedgehog.model.graph.sub.Order;

import java.util.Optional;
import java.util.Set;

public class ReleaseTimeCalc {

    private final Graph graph;

    public ReleaseTimeCalc(Graph graph) {
        this.graph = graph;
    }

    public int getReleaseTimeFor(String currentMachine, int endingOrderId) {
        int releaseTime = 0;
        for(Order order : graph.getOrders()) {
            Optional<Edge> startingEdge = graph.getStartEdgeFor(order.getOrderId());
            if(startingEdge.isPresent()) {
                Set<Edge> nextEdges = graph.getNextEdgesFor(startingEdge.get());
                for(Edge nextEdge : nextEdges) {
                    int nextReleaseTime = calculateReleaseTimeForSubgraph(nextEdge, currentMachine, endingOrderId, 0);
                    if(nextReleaseTime > releaseTime) {
                        releaseTime = nextReleaseTime;
                    }
                }
            }
        }
        return releaseTime;
    }

    private int calculateReleaseTimeForSubgraph(Edge edge, String currentMachine, int endingOrderId, int currentReleaseTime) {
        currentReleaseTime += edge.getWeight();
        Set<Edge> nextEdges = graph.getNextEdgesFor(edge);
        int maxCurrentReleaseTime = 0;
        int stableCurrentReleaseTime = currentReleaseTime;
        for(Edge nextEdge : nextEdges) {
            Optional<Edge> neededEdge = graph.getLastEdgeFor(currentMachine, endingOrderId);
            if(neededEdge.isPresent() && neededEdge.get().equals(nextEdge)) {
                return currentReleaseTime;
            }

            int nextCurrentReleaseTime = calculateReleaseTimeForSubgraph(nextEdge, currentMachine, endingOrderId, stableCurrentReleaseTime);
            if(nextCurrentReleaseTime > maxCurrentReleaseTime) {
                maxCurrentReleaseTime = nextCurrentReleaseTime;
            }
        }
        return maxCurrentReleaseTime;
    }

    public int getReleaseTimeForEdge(Edge neededEdge) {
        int releaseTime = 0;
        for(Order order : graph.getOrders()) {
            Optional<Edge> startingEdge = graph.getStartEdgeFor(order.getOrderId());
            if(startingEdge.isPresent()) {
                Set<Edge> nextEdges = graph.getNextEdgesFor(startingEdge.get());
                for(Edge nextEdge : nextEdges) {
                    int nextReleaseTime = calculateReleaseTimeForSubgraph(nextEdge, neededEdge, 0);
                    if(nextReleaseTime > releaseTime) {
                        releaseTime = nextReleaseTime;
                    }
                }
            }
        }
        return releaseTime;
    }

    private int calculateReleaseTimeForSubgraph(Edge edge, Edge neededEdge, int currentReleaseTime) {
        currentReleaseTime += edge.getWeight();
        Set<Edge> nextEdges = graph.getNextEdgesFor(edge);
        int maxCurrentReleaseTime = 0;
        int stableCurrentReleaseTime = currentReleaseTime;
        for(Edge nextEdge : nextEdges) {

            if(neededEdge.equals(nextEdge)) {
                return currentReleaseTime;
            }

            int nextCurrentReleaseTime = calculateReleaseTimeForSubgraph(nextEdge, neededEdge, stableCurrentReleaseTime);
            if(nextCurrentReleaseTime > maxCurrentReleaseTime) {
                maxCurrentReleaseTime = nextCurrentReleaseTime;
            }
        }

        return maxCurrentReleaseTime;
    }

    public int getReleaseTimeIncludingBlockingFor(String currentMachine, int endingOrderId) {
        int releaseTime = 0;
        for(Order order : graph.getOrders()) {
            Optional<Edge> startingEdge = graph.getStartEdgeFor(order.getOrderId());
            if(startingEdge.isPresent()) {
                Set<Edge> nextEdges = graph.getNextEdgesFor(startingEdge.get());
                for(Edge nextEdge : nextEdges) {
                    int nextReleaseTime = calculateReleaseTimeIncludingBlockingForSubgraph(nextEdge, currentMachine, endingOrderId, 0);
                    if(nextReleaseTime > releaseTime) {
                        releaseTime = nextReleaseTime;
                    }
                }
            }
        }
        return releaseTime;
    }

    private int calculateReleaseTimeIncludingBlockingForSubgraph(Edge edge, String currentMachine, int endingOrderId, int currentReleaseTime) {
        currentReleaseTime += edge.getWeight();
        Set<Edge> nextEdges = graph.getNextEdgesFor(edge);
        int maxCurrentReleaseTime = 0;
        int stableCurrentReleaseTime = currentReleaseTime;
        for(Edge nextEdge : nextEdges) {
            Optional<Edge> nextEdgeWithSameMachine = nextEdges.stream().filter(nEdge -> edge.getFirstNode().getMachine().equals(nEdge.getFirstNode().getMachine())).findFirst();
            if(nextEdgeWithSameMachine.isPresent() && !nextEdge.equals(nextEdgeWithSameMachine.get())) {
                currentReleaseTime += nextEdgeWithSameMachine.get().getWeight();
                stableCurrentReleaseTime += nextEdgeWithSameMachine.get().getWeight();
            }
            Optional<Edge> neededEdge = graph.getLastEdgeFor(currentMachine, endingOrderId);
            if(neededEdge.isPresent() && neededEdge.get().equals(nextEdge)) {
                return currentReleaseTime;
            }
            int nextCurrentReleaseTime = calculateReleaseTimeIncludingBlockingForSubgraph(nextEdge, currentMachine, endingOrderId, stableCurrentReleaseTime);
            if(nextCurrentReleaseTime > maxCurrentReleaseTime) {
                maxCurrentReleaseTime = nextCurrentReleaseTime;
            }
        }
        return maxCurrentReleaseTime;
    }
}
