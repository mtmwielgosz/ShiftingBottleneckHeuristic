package systems.hedgehog.model.graph;

import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.graph.subelement.Node;
import systems.hedgehog.model.graph.subelement.ResultSubgraph;
import systems.hedgehog.model.graph.subelement.Subgraph;
import systems.hedgehog.model.struct.OrderInGraph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Graph {

    protected Set<Edge> edges = new HashSet<>();

    protected Set<OrderInGraph> orders = new HashSet<>();
    protected String stringGraph;

    public static Node startNode;
    public static Node endNode;
    static {
        startNode = new Node("U", "", 0);
        endNode = new Node("V", "", 0);
    }

    protected void setEdges(Set<Edge> edges) {
        this.edges = edges;
    }

    protected void setOrders(Set<OrderInGraph> orders) {
        this.orders = orders;
    }

    public Optional<Edge> getStartEdgeFor(int orderId) {
        return edges.stream().filter(edge -> getOrder(orderId).get().getJobsInOrder().contains(edge))
                .filter(edge -> edge.getFirstNode().equals(Graph.startNode)).findFirst();
    }

    public Optional<Edge> getNextEdgeFor(Edge currentEdge, int orderId) {
        if(currentEdge.getSecondNode().equals(Graph.endNode)) {
            return Optional.empty();
        }
        return edges.stream().filter(edge -> getOrder(orderId).get().getJobsInOrder().contains(edge))
                .filter(edge -> currentEdge.getSecondNode().equals(edge.getFirstNode())).findFirst();
    }

    public Optional<Edge> getNextEdgeFor(String currentMachine, List<Edge> notInEdge, int notOrderId) {
        for(OrderInGraph currentOrder : orders) {
            if(currentOrder.getOrderId() != notOrderId) {
                return edges.stream().filter(edge -> currentMachine.equals(edge.getFirstNode().getMachine()))
                        .filter(edge -> !notInEdge.contains(edge))
                        .findFirst();
            }
        }
        return Optional.empty();
    }

    public Set<Edge> getNextEdgesFor(Edge currentEdge) {
        if(currentEdge.getSecondNode().equals(SecGraph.endNode)) {
            return new HashSet<>();
        }
        return edges.stream().filter(edge -> currentEdge.getSecondNode().equals(edge.getFirstNode())).collect(Collectors.toSet());
    }

    public int getMakespan() {
        int maxMakespan = 0;
        for(OrderInGraph order : orders) {
            Optional<Edge> startingEdge = getStartEdgeFor(order.getOrderId());
            if(startingEdge.isPresent()) {
                Set<Edge> nextEdges = getNextEdgesFor(startingEdge.get());
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
        if(endNode.equals(edge)) {
            return currentMakespan;
        }
        currentMakespan += edge.getWeight();
        Set<Edge> nextEdges = getNextEdgesFor(edge);
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


    public int getMakespanForOrder(int endingOrderId) {
        int maxMakespan = 0;
       for(OrderInGraph startingOrder : orders) {
           Optional<Edge> startingEdge = getStartEdgeFor(startingOrder.getOrderId());
           if (startingEdge.isPresent()) {
               Set<Edge> nextEdges = getNextEdgesFor(startingEdge.get());
               for (Edge edge : nextEdges) {
                   int nextMakespan = calculateMakespanForSubgraph(edge, endingOrderId, 0);
                   if (nextMakespan > maxMakespan) {
                       maxMakespan = nextMakespan;
                   }
               }
           }
       }

        return maxMakespan;
    }

    private int calculateMakespanForSubgraph(Edge edge, int endingOrderId, int currentMakespan) {

        currentMakespan += edge.getWeight();
        Set<Edge> nextEdges = getNextEdgesFor(edge);
        int maxCurrentMakespan = 0;
        int stableCurrentMakespan = currentMakespan;
        for(Edge nextEdge : nextEdges) {

            Optional<Edge> nextEdgeWithSameMachine = nextEdges.stream().filter(nEdge -> edge.getFirstNode().getMachine().equals(nEdge.getFirstNode().getMachine())).findFirst();
            if(nextEdgeWithSameMachine.isPresent() && !nextEdge.equals(nextEdgeWithSameMachine.get())) {
                currentMakespan += nextEdgeWithSameMachine.get().getWeight();
                stableCurrentMakespan += nextEdgeWithSameMachine.get().getWeight();
            }

            if(endNode.equals(nextEdge.getSecondNode()) && getOrder(nextEdge).isPresent() && getOrder(nextEdge).get().getOrderId() == endingOrderId) {
                return currentMakespan + nextEdge.getWeight();
            }

            int nextCurrentMakespan = calculateMakespanForSubgraph(nextEdge, endingOrderId, stableCurrentMakespan);
            if(nextCurrentMakespan > maxCurrentMakespan) {
                maxCurrentMakespan = nextCurrentMakespan;
            }
        }
        return maxCurrentMakespan;
    }

    public Edge addEdge(int orderId, Node srcNode, Node destNode) {
        Edge newEdge = new Edge(srcNode, destNode, srcNode.getWeightToNextNode());
        edges.add(newEdge);
        Optional<OrderInGraph> currentOrder = getOrder(orderId);
        if(currentOrder.isPresent()) {
            currentOrder.get().addJob(newEdge);
        } else {
            OrderInGraph newOrderInGraph = new OrderInGraph(orderId);
            newOrderInGraph.addJob(newEdge);
            orders.add(newOrderInGraph);
        }
        return edges.stream().filter(edge -> edge.equals(newEdge)).findFirst().orElse(null);
    }

    protected Optional<OrderInGraph> getOrder(int orderId) {
        return orders.stream().filter(order -> order.getOrderId() == orderId).findFirst();
    }

    public Optional<OrderInGraph> getOrder(Edge edge) {
        return orders.stream().filter(order -> order.getJobsInOrder().contains(edge)).findFirst();
    }

    public int getProductionTimeFor2(String currentMachine, int orderId) {
        return edges.stream().filter(edge -> getOrder(orderId).get().getJobsInOrder().contains(edge))
                .filter(edge -> currentMachine.equals(edge.getFirstNode().getMachine()))
                .flatMapToInt(edge -> IntStream.of(edge.getWeight())).sum();
    }

    public int getRealProductionTimeFor(String currentMachine, int orderId) {
        int maxProductionTime =  getProductionTimeFor2(currentMachine, orderId);
        int releaseTimeCurrent =  getRealReleaseTimeFor2(currentMachine, orderId);

        Optional<Edge> currentEdge = edges.stream().filter(edge -> getOrder(orderId).get().getJobsInOrder().contains(edge))
                .filter(edge -> currentMachine.equals(edge.getFirstNode().getMachine())).findFirst();
        if(currentEdge.isPresent()) {
            Set<Edge> nextEdges = getNextEdgesFor(currentEdge.get());
            for(Edge nextEdge : nextEdges) {
                int releaseTimeForNextEdge = getRealReleaseTimeFor2(nextEdge.getFirstNode().getMachine(), orderId);
                if(releaseTimeForNextEdge > releaseTimeCurrent + maxProductionTime) {
                    maxProductionTime = releaseTimeForNextEdge - releaseTimeCurrent;
                }
            }
        }

        return maxProductionTime;
    }


    public int getReleaseTimeFor2(String currentMachine, int endingOrderId) {
        int releaseTime = 0;
        for(OrderInGraph order : orders) {
            Optional<Edge> startingEdge = getStartEdgeFor(order.getOrderId());
            if(startingEdge.isPresent()) {
                Set<Edge> nextEdges = getNextEdgesFor(startingEdge.get());
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
        Set<Edge> nextEdges = getNextEdgesFor(edge);
        int maxCurrentReleaseTime = 0;
        int stableCurrentReleaseTime = currentReleaseTime;
        for(Edge nextEdge : nextEdges) {

            if(currentMachine.equals(nextEdge.getFirstNode().getMachine()) && getOrder(nextEdge).isPresent() && getOrder(nextEdge).get().getOrderId() == endingOrderId) {
                return currentReleaseTime;
            }

            int nextCurrentReleaseTime = calculateReleaseTimeForSubgraph(nextEdge, currentMachine, endingOrderId, stableCurrentReleaseTime);
            if(nextCurrentReleaseTime > maxCurrentReleaseTime) {
                maxCurrentReleaseTime = nextCurrentReleaseTime;
            }
        }

        return maxCurrentReleaseTime;
    }

    public int getRealReleaseTimeFor2(String currentMachine, int endingOrderId) {
        int releaseTime = 0;
        for(OrderInGraph order : orders) {
            Optional<Edge> startingEdge = getStartEdgeFor(order.getOrderId());
            if(startingEdge.isPresent()) {
                Set<Edge> nextEdges = getNextEdgesFor(startingEdge.get());
                for(Edge nextEdge : nextEdges) {
                    int nextReleaseTime = calculateRealReleaseTimeForSubgraph(nextEdge, currentMachine, endingOrderId, 0);
                    if(nextReleaseTime > releaseTime) {
                        releaseTime = nextReleaseTime;
                    }
                }
            }
        }
        return releaseTime;
    }

    private int calculateRealReleaseTimeForSubgraph(Edge edge, String currentMachine, int endingOrderId, int currentReleaseTime) {

        currentReleaseTime += edge.getWeight();
        Set<Edge> nextEdges = getNextEdgesFor(edge);
        int maxCurrentReleaseTime = 0;
        int stableCurrentReleaseTime = currentReleaseTime;
        for(Edge nextEdge : nextEdges) {

            Optional<Edge> nextEdgeWithSameMachine = nextEdges.stream().filter(nEdge -> edge.getFirstNode().getMachine().equals(nEdge.getFirstNode().getMachine())).findFirst();
            if(nextEdgeWithSameMachine.isPresent() && !nextEdge.equals(nextEdgeWithSameMachine.get())) {
                currentReleaseTime += nextEdgeWithSameMachine.get().getWeight();
                stableCurrentReleaseTime += nextEdgeWithSameMachine.get().getWeight();
            }

            if(currentMachine.equals(nextEdge.getFirstNode().getMachine()) && getOrder(nextEdge).isPresent() && getOrder(nextEdge).get().getOrderId() == endingOrderId) {
                return currentReleaseTime;
            }

            int nextCurrentReleaseTime = calculateReleaseTimeForSubgraph(nextEdge, currentMachine, endingOrderId, stableCurrentReleaseTime);
            if(nextCurrentReleaseTime > maxCurrentReleaseTime) {
                maxCurrentReleaseTime = nextCurrentReleaseTime;
            }
        }

        return maxCurrentReleaseTime;
    }

    public int getReleaseTimeFor(Edge edge, int orderId) {
        Optional<Edge> currentEdge = getStartEdgeFor(orderId);
        int releaseTime = 0;
        while(currentEdge.isPresent() && !edge.equals(currentEdge.get())) {
            releaseTime += currentEdge.get().getWeight();
            currentEdge = getNextEdgeFor(currentEdge.get(), orderId);
        }
        return releaseTime;
    }

    public int getDueDateFor(String currentMachine, int orderId) {

        Optional<Edge> currentEdge = getStartEdgeFor(orderId);
        while(currentEdge.isPresent() && !currentMachine.equals(currentEdge.get().getFirstNode().getMachine())) {
            currentEdge = getNextEdgeFor(currentEdge.get(), orderId);
        }
        if(currentEdge.isPresent()) {
            currentEdge = getNextEdgeFor(currentEdge.get(), orderId);
        }

        int succeedingProcessingTime = 0;
        while(currentEdge.isPresent()) {
            succeedingProcessingTime += currentEdge.get().getWeight();
            currentEdge = getNextEdgeFor(currentEdge.get(), orderId);
        }

        return getMakespan() - succeedingProcessingTime;
    }

    public int getDueDateFor2(String currentMachine, int endingOrderId) {

        return getMakespan() - getMakespanForOrder(endingOrderId) + getRealProductionTimeFor(currentMachine, endingOrderId) + getRealReleaseTimeFor2(currentMachine, endingOrderId);
    }

    protected List<Edge> getMaxLatenessSubgraphFor(String currentMachine, int orderId, int currentMakespan, List<Edge> visitedEdges) {
        Optional<Edge> currentEdge = getNextEdgeFor(currentMachine, visitedEdges, orderId);
        if(currentEdge.isPresent()) {
            visitedEdges.add(currentEdge.get());
            currentMakespan += currentEdge.get().getWeight();
            visitedEdges = getMaxLatenessSubgraphFor(currentMachine, orderId, currentMakespan, visitedEdges);
        }
        return visitedEdges;
    }

    public List<Edge> getLatenessSubgraph(String currentMachine, int startingOrderId, int endingOrderId) {
        List<Edge> visitedEdges = new LinkedList<>();
        int currentMakespan = 0;
        if (startingOrderId != endingOrderId) {
            Optional<Edge> currentEdge = getStartEdgeFor(startingOrderId);
            currentEdge = getNextEdgeFor(currentEdge.get(), startingOrderId);
            List<Edge> subGraph = new LinkedList<>();
            while(currentEdge.isPresent() && !currentMachine.equals(currentEdge.get().getFirstNode().getMachine())) {
                subGraph.add(currentEdge.get());
                currentEdge = getNextEdgeFor(currentEdge.get(), startingOrderId);
            }
            if(currentEdge.isPresent()) {
                visitedEdges.add(currentEdge.get());
                currentMakespan += currentEdge.get().getWeight();
            }
            subGraph.addAll(getMaxLatenessSubgraphFor(currentMachine, startingOrderId, currentMakespan, visitedEdges));
            addSucceedingEdges(endingOrderId, subGraph);
            return subGraph;
        }
        return new LinkedList<>();
    }

    public List<Subgraph> getMaximumLatenessSubgraphsFor(String currentMachine) {
        int minimalizedMaxMakespan = Integer.MAX_VALUE;
        List<Subgraph> subgraphs = new ArrayList<>();
        for(OrderInGraph endingOrder : orders) {
            List<Edge> graphToAdd = new LinkedList<>();
            for(OrderInGraph startingOrder: orders) {
                List<Edge> currentLatenessSubgraph = getLatenessSubgraph(currentMachine, startingOrder.getOrderId(), endingOrder.getOrderId());
                if(!currentLatenessSubgraph.isEmpty()) {
                    int currentMaxMakespan = getCurrentMaxMakespan(currentLatenessSubgraph);
                    if (currentMaxMakespan < minimalizedMaxMakespan) {
                        minimalizedMaxMakespan = currentMaxMakespan;
                        graphToAdd = currentLatenessSubgraph;
                    }
                }
            }
            subgraphs.add(new Subgraph(endingOrder.getOrderId(), graphToAdd, minimalizedMaxMakespan));
            minimalizedMaxMakespan = Integer.MAX_VALUE;
        }
        return subgraphs;
    }

    protected int getCurrentMaxMakespan(List<Edge> currentLatenessSubgraph) {

        int maxMakespan = 0;
        for(int index = 0; index < currentLatenessSubgraph.size() - 1; index++) {
            Edge currentEdge = currentLatenessSubgraph.get(index);
            Optional<OrderInGraph> currentOrder = orders.stream().filter(order -> order.getJobsInOrder().contains(currentEdge)).findFirst();
            Edge nextEdge = currentLatenessSubgraph.get(index + 1);
            Optional<OrderInGraph> nextOrder = orders.stream().filter(order -> order.getJobsInOrder().contains(nextEdge)).findFirst();
            if (currentOrder.isPresent() && nextOrder.isPresent()) {
                int releaseTimeOfCurrentEdge = getReleaseTimeFor(currentEdge, currentOrder.get().getOrderId());
                int releaseTimeOfNextEdge = getReleaseTimeFor(nextEdge, nextOrder.get().getOrderId());
                if (releaseTimeOfNextEdge > releaseTimeOfCurrentEdge + currentEdge.getWeight()) {
                    maxMakespan += releaseTimeOfNextEdge - releaseTimeOfCurrentEdge;
                } else {
                    maxMakespan += currentEdge.getWeight();
                }
            }
        }
        return maxMakespan + currentLatenessSubgraph.get(currentLatenessSubgraph.size() - 1).getWeight();
    }

    public List<Subgraph> getReducedMaximumLatenessSubgraphsFor(String currentMachine) {
        List<Subgraph> subgraphs = getMaximumLatenessSubgraphsFor(currentMachine);
        List<Subgraph> minimalizedSubgraphs = new LinkedList<>();
        Subgraph minimalizedMaxLateness = subgraphs.stream().min(Comparator.comparing(Subgraph::getMaxMakespan)).get();
        for(OrderInGraph currentOrder : orders) {
            List<Edge> minimalizedEdges = new ArrayList<>(minimalizedMaxLateness.getEdges());
            Optional<Edge> lastEdge = minimalizedEdges.stream().filter(edge -> currentOrder.getJobsInOrder().contains(edge)).findFirst();
            if (lastEdge.isPresent()) {
                minimalizedEdges = minimalizedEdges.subList(0, minimalizedEdges.indexOf(lastEdge.get()) + 1);
                Optional<Edge> nextEdge = getNextEdgeFor(lastEdge.get(), currentOrder.getOrderId());
                while (nextEdge.isPresent()) {
                    minimalizedEdges.add(nextEdge.get());
                    nextEdge = getNextEdgeFor(nextEdge.get(), currentOrder.getOrderId());
                }
            }
            int minimizedMaxMakespan = getCurrentMaxMakespan(minimalizedEdges);
            minimalizedSubgraphs.add(new Subgraph(currentOrder.getOrderId(), minimalizedEdges, minimizedMaxMakespan));
        }
        return minimalizedSubgraphs;
    }

    public ResultSubgraph getRealMaxLatenessFor(String currentMachine, int startingOrderId) {

        int currentMakespan = getCurrentMakespan(currentMachine, startingOrderId);
        int maxLateness = getDueDateFor2(currentMachine, startingOrderId) - currentMakespan;
        List<OrderInGraph> visitedOrders = new LinkedList<>();
        visitedOrders.add(getOrder(startingOrderId).get());
        int realMaxLateness = getRealMaxLatenessRecFor(currentMachine, visitedOrders, currentMakespan, maxLateness);
        return new ResultSubgraph(visitedOrders, realMaxLateness);

    }

    private int getRealMaxLatenessRecFor(String currentMachine, List<OrderInGraph> visitedOrders, int currentMakespan, int maxLateness) {

        if(visitedOrders.containsAll(orders)) {
            return maxLateness;
        }

        List<OrderInGraph> copyOfVisitedOrders = new LinkedList<>(visitedOrders);
        for(OrderInGraph nextOrder : orders) {
            if (!copyOfVisitedOrders.contains(nextOrder)) {
                int nextReleaseTime = getReleaseTimeFor2(currentMachine, nextOrder.getOrderId());
                if (currentMakespan < nextReleaseTime) {
                    currentMakespan = nextReleaseTime;
                }
                currentMakespan += getProductionTimeFor2(currentMachine, nextOrder.getOrderId());
                int nextLateness = currentMakespan - getDueDateFor2(currentMachine, nextOrder.getOrderId());
                if (nextLateness > maxLateness) {
                    maxLateness = nextLateness;
                }
                copyOfVisitedOrders.add(nextOrder);
                maxLateness = getRealMaxLatenessRecFor(currentMachine, copyOfVisitedOrders, currentMakespan, maxLateness);
            }
        }

        visitedOrders = copyOfVisitedOrders;
        return maxLateness;
    }

    private int getCurrentMakespan(String currentMachine, int startingOrderId) {
        return getReleaseTimeFor2(currentMachine, startingOrderId) + getProductionTimeFor2(currentMachine, startingOrderId);
    }

    public int getMinimizedLatenessFor(String currentMachine, int orderId) {
        List<Subgraph> minimizedSubgraph = getReducedMaximumLatenessSubgraphsFor(currentMachine);
        return minimizedSubgraph.stream().filter(subgraph -> subgraph.getOrderId() == orderId)
                .flatMapToInt(subgraph -> IntStream.of(subgraph.getMaxMakespan())).sum() - getMakespan();

    }

    private void addSucceedingEdges(int endingOrderId, List<Edge> subGraph) {
        Optional<OrderInGraph> endingOrder = getOrder(endingOrderId);
        if (endingOrder.isPresent()) {
            Optional<Edge> lastEdge = subGraph.stream().filter(edge -> endingOrder.get().getJobsInOrder().contains(edge)).findFirst();
            if (lastEdge.isPresent()) {
                for (int index = subGraph.indexOf(lastEdge.get()) + 1; index < subGraph.size(); index++) {
                    Edge toSwap = subGraph.get(index);
                    subGraph.set(index - 1, toSwap);
                    subGraph.set(index, lastEdge.get());
                }
                Optional<Edge> nextEdge = getNextEdgeFor(lastEdge.get(), endingOrderId);
                while (nextEdge.isPresent()) {
                    subGraph.add(nextEdge.get());
                    nextEdge = getNextEdgeFor(nextEdge.get(), endingOrderId);
                }
            }
        }
    }

    public void setString(String stringGraph) {
        this.stringGraph = stringGraph;
    }

    @Override
    public String toString() {
        return stringGraph;
    }
}
