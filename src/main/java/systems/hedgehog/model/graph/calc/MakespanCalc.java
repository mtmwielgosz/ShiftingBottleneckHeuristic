package systems.hedgehog.model.graph.calc;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.sub.Edge;
import systems.hedgehog.model.result.MakespanResult;
import systems.hedgehog.model.graph.sub.Order;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MakespanCalc {

    private final Graph graph;

    public MakespanCalc(Graph graph) {
        this.graph = graph;
    }

    public int getCurrentMakespan(String currentMachine, int startingOrderId) {
        return graph.getReleaseTimeIncludingBlockingFor(currentMachine, startingOrderId) + graph.getProductionTimeFor(currentMachine, startingOrderId);
    }

    public int getMakespan() {
        int maxMakespan = 0;
        for(Order order : graph.getOrders()) {
            Optional<Edge> startingEdge = graph.getStartEdgeFor(order.getOrderId());
            if(startingEdge.isPresent()) {
                Set<Edge> nextEdges = graph.getNextEdgesFor(startingEdge.get());
                for(Edge edge : nextEdges) {
                    int nextMakespan = calculateMakespanForSubgraph(edge, 0);
                    if(nextMakespan > maxMakespan) {
                        maxMakespan = nextMakespan;
                    }
                }
            }
        }
        return maxMakespan;
    }

    private int calculateMakespanForSubgraph(Edge edge, int currentMakespan) {
        if(Graph.endNode.equals(edge.getFirstNode())) {
            return currentMakespan;
        }
        currentMakespan += edge.getWeight();
        Set<Edge> nextEdges = graph.getNextEdgesFor(edge);
        int maxCurrentMakespan = currentMakespan;
        int stableCurrentMakespan = currentMakespan;
        for(Edge nextEdge : nextEdges) {
            int nextCurrentMakespan = calculateMakespanForSubgraph(nextEdge, stableCurrentMakespan);
            if(nextCurrentMakespan > maxCurrentMakespan) {
                maxCurrentMakespan = nextCurrentMakespan;
            }
        }
        return maxCurrentMakespan;
    }

    public MakespanResult getMakespanForEndingOrder(int endingOrderId) {
        int maxMakespan = 0;
        List<Edge> resultEdges = new LinkedList<>();
        for(Order startingOrder : graph.getOrders()) {
            Optional<Edge> startingEdge = graph.getStartEdgeFor(startingOrder.getOrderId());
            if (startingEdge.isPresent()) {
                Set<Edge> nextEdges = graph.getNextEdgesFor(startingEdge.get());
                for (Edge edge : nextEdges) {
                    MakespanResult nextMakespan = calculateMakespanForSubgraph(edge, endingOrderId, 0, new LinkedList<>());
                    if (nextMakespan.getMakespan() > maxMakespan) {
                        maxMakespan = nextMakespan.getMakespan();
                        resultEdges = nextMakespan.getEdges();
                    }
                }
            }
        }
        return new MakespanResult(maxMakespan, resultEdges);
    }

    private MakespanResult calculateMakespanForSubgraph(Edge edge, int endingOrderId, int currentMakespan, List<Edge> currentEdges) {
        currentMakespan += edge.getWeight();
        currentEdges.add(edge);
        Set<Edge> nextEdges = graph.getNextEdgesFor(edge);
        int maxCurrentMakespan = 0;
        List<Edge> maxCurrentEdges = new LinkedList<>();
        int stableCurrentMakespan = currentMakespan;
        List<Edge> stableCurrentEdges = new LinkedList<>(currentEdges);
        for(Edge nextEdge : nextEdges) {

            Optional<Edge> nextEdgeWithSameMachine = nextEdges.stream().filter(nEdge -> edge.getFirstNode().getMachine().equals(nEdge.getFirstNode().getMachine())).findFirst();
            if(nextEdgeWithSameMachine.isPresent() && !nextEdge.equals(nextEdgeWithSameMachine.get())) {
                currentEdges.add(nextEdgeWithSameMachine.get());
                currentMakespan += nextEdgeWithSameMachine.get().getWeight();
                stableCurrentEdges.add(nextEdgeWithSameMachine.get());
                stableCurrentMakespan += nextEdgeWithSameMachine.get().getWeight();
            }

            if(Graph.endNode.equals(nextEdge.getSecondNode()) && graph.getOrder(nextEdge).isPresent() && graph.getOrder(nextEdge).get().getOrderId() == endingOrderId) {
                currentEdges.add(nextEdge);
                currentMakespan += nextEdge.getWeight();
                return new MakespanResult(currentMakespan, currentEdges);
            }

            MakespanResult nextCurrentMakespan = calculateMakespanForSubgraph(nextEdge, endingOrderId, stableCurrentMakespan, stableCurrentEdges);
            if(nextCurrentMakespan.getMakespan() > maxCurrentMakespan) {
                maxCurrentMakespan = nextCurrentMakespan.getMakespan();
                maxCurrentEdges = nextCurrentMakespan.getEdges();
            }
        }
        return new MakespanResult(maxCurrentMakespan, maxCurrentEdges);
    }

}
