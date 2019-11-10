package systems.hedgehog.model.graph.calc;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.sub.Edge;
import systems.hedgehog.model.result.SubgraphResult;
import systems.hedgehog.model.graph.sub.Order;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MaxLatenessCalc {

    private final Graph graph;

    public MaxLatenessCalc(Graph graph) {
        this.graph = graph;
    }

    public SubgraphResult getMinimizedMaxLatenessResultFor(String currentMachine) {
        int minLateness = Integer.MAX_VALUE;
        SubgraphResult subgraphResult = null;
        List<Order> allOrdersForMachine = graph.getOrdersWithMachine(currentMachine);
        for(Order order : allOrdersForMachine) {
            SubgraphResult currentResult = getMaxLatenessFor(currentMachine, order.getOrderId());
            if(currentResult.getMaxLateness() < minLateness) {
                minLateness = currentResult.getMaxLateness();
                subgraphResult = currentResult;
            }
        }
        return subgraphResult;
    }

    private SubgraphResult getMaxLatenessFor(String currentMachine, int startingOrderId) {
        int currentMakespan = graph.getCurrentMakespan(currentMachine, startingOrderId);
        int maxLateness = currentMakespan - graph.getDueDateFor(currentMachine, startingOrderId);
        List<Edge> visitedEdges = new LinkedList<>();
        Optional<Edge> firstEdge = graph.getFirstEdgeFor(currentMachine, startingOrderId);
        firstEdge.ifPresent(visitedEdges::add);
        return getMaxLatenessRecFor(currentMachine, visitedEdges, currentMakespan, maxLateness, new LinkedList<>(visitedEdges), Integer.MAX_VALUE);
    }

    private SubgraphResult getMaxLatenessRecFor(String currentMachine, List<Edge> visitedEdges, int currentMakespan, int maxLateness, List<Edge> resultEdges, int minLateness) {
        List<Edge> allEdgesWithMachine = graph.getAllEdgesWithMachine(currentMachine);
        if(visitedEdges.containsAll(allEdgesWithMachine)) {
            return new SubgraphResult(visitedEdges, maxLateness, currentMachine);
        }
        List<Edge> copyOfvisitedEdges = new LinkedList<>(visitedEdges);
        for(Edge nextEdge : allEdgesWithMachine) {
            List<Edge> deepCopyOfVisitedEdges = new LinkedList<>(copyOfvisitedEdges);
            int deepCopyOfCurrentMakespan = currentMakespan;
            int deepCopyOfMaxLateness = maxLateness;
            if (!deepCopyOfVisitedEdges.contains(nextEdge)) {
                deepCopyOfVisitedEdges.add(nextEdge);
                int nextReleaseTime = graph.getReleaseTimeForEdge(nextEdge);
                if (deepCopyOfCurrentMakespan < nextReleaseTime) {
                    deepCopyOfCurrentMakespan = nextReleaseTime;
                }
                deepCopyOfCurrentMakespan += nextEdge.getWeight();
                int nextLateness = deepCopyOfCurrentMakespan -  graph.getDueDateForEdge(nextEdge);
                SubgraphResult recResult = getMaxLatenessRecFor(currentMachine, deepCopyOfVisitedEdges, deepCopyOfCurrentMakespan, nextLateness, resultEdges, minLateness);
                int currentMaxLateness = recResult.getMaxLateness();
                deepCopyOfVisitedEdges = recResult.getResultSubgraph();
                if (currentMaxLateness > deepCopyOfMaxLateness) {
                    deepCopyOfMaxLateness = currentMaxLateness;
                }
                if(minLateness > deepCopyOfMaxLateness) {
                    minLateness = deepCopyOfMaxLateness;
                    resultEdges = deepCopyOfVisitedEdges;
                }
            }
        }
        return new SubgraphResult(resultEdges, minLateness, currentMachine);
    }
}
