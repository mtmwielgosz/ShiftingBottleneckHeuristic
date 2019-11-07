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

    public Set<OrderInGraph> getOrders() {
        return orders;
    }

    public Optional<Edge> getEdgeFor(String currentMachine, int orderId) {
        return edges.stream().filter(edge -> currentMachine.equals(edge.getFirstNode().getMachine()))
                .filter(edge -> getOrder(edge).isPresent() && getOrder(edge).get().getOrderId() == orderId)
                .findFirst();
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

    public Optional<Edge> getEdgeFor(String currentMachine, List<Edge> notInEdge, int notOrderId) {
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
                .flatMapToInt(edge -> IntStream.of(edge.getWeight())).sum(); // todo sum of last
    }

    public int getRealProductionTimeFor(String currentMachine, int orderId) { // todo prod for last of machines
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

    private int calculateReleaseTimeForSubgraph(Edge edge, String currentMachine, int endingOrderId, int currentReleaseTime) { // todo for last of machines

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

    private int calculateRealReleaseTimeForSubgraph(Edge edge, String currentMachine, int endingOrderId, int currentReleaseTime) { // todo for last of machines

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

    public int getDueDateFor2(String currentMachine, int endingOrderId) { // todo for last of machines

        return getMakespan() - getMakespanForOrder(endingOrderId) + getRealProductionTimeFor(currentMachine, endingOrderId) + getRealReleaseTimeFor2(currentMachine, endingOrderId);
    }


    public ResultSubgraph getRealMinimizedMaxLatenessFor(String currentMachine) { // todo for last of machines

        int minLateness = Integer.MAX_VALUE;
        ResultSubgraph resSubgraph = null;
        List<OrderInGraph> allOrdersForMachine = orders.stream().filter(
                order -> order.getJobsInOrder().stream().anyMatch(job -> currentMachine.equals(job.getFirstNode().getMachine()))).collect(Collectors.toList());
        for(OrderInGraph order : allOrdersForMachine) {
            ResultSubgraph currentRes = getRealMaxLatenessFor(currentMachine, order.getOrderId());
            if(currentRes.getMaxLateness() < minLateness) {
                minLateness = currentRes.getMaxLateness();
                resSubgraph = currentRes;
            }
        }
        return resSubgraph;
    }

    public ResultSubgraph getRealMaxLatenessFor(String currentMachine, int startingOrderId) { // todo for last of machines

        int currentMakespan = getCurrentMakespan(currentMachine, startingOrderId);
        int maxLateness = currentMakespan - getDueDateFor2(currentMachine, startingOrderId);

        List<OrderInGraph> visitedOrders = new LinkedList<>();
        visitedOrders.add(getOrder(startingOrderId).get());
        return getRealMaxLatenessRecFor(currentMachine, visitedOrders, currentMakespan, maxLateness, new LinkedList<>(visitedOrders), Integer.MAX_VALUE);
    }

    private ResultSubgraph getRealMaxLatenessRecFor(String currentMachine, List<OrderInGraph> visitedOrders, int currentMakespan, int maxLateness, List<OrderInGraph> resultOrders, int minLateness) {
// todo for last of machines

        List<OrderInGraph> allOrdersForMachine = orders.stream().filter(
                order -> order.getJobsInOrder().stream().anyMatch(job -> currentMachine.equals(job.getFirstNode().getMachine()))).collect(Collectors.toList());
        if(visitedOrders.containsAll(allOrdersForMachine)) {
            return new ResultSubgraph(visitedOrders, maxLateness, currentMachine);
        }

        List<OrderInGraph> copyOfvisitedOrders = new LinkedList<>(visitedOrders);
        int copyOfCurrentMakespan = currentMakespan;
        int copyOfMaxLateness = maxLateness;
        for(OrderInGraph nextOrder : orders) {
            List<OrderInGraph> deepCopyOfVisitedOrders = new LinkedList<>(copyOfvisitedOrders);
            int deepCopyOfCurrentMakespan = copyOfCurrentMakespan;
            int deepCopyOfMaxLateness = copyOfMaxLateness;
            if (!deepCopyOfVisitedOrders.contains(nextOrder)) {
                deepCopyOfVisitedOrders.add(nextOrder);
                int nextReleaseTime = getReleaseTimeFor2(currentMachine, nextOrder.getOrderId());
                if (deepCopyOfCurrentMakespan < nextReleaseTime) {
                    deepCopyOfCurrentMakespan = nextReleaseTime;
                }
                deepCopyOfCurrentMakespan += getProductionTimeFor2(currentMachine, nextOrder.getOrderId());
                int nextLateness = deepCopyOfCurrentMakespan - getDueDateFor2(currentMachine, nextOrder.getOrderId());
                ResultSubgraph recResult = getRealMaxLatenessRecFor(currentMachine, deepCopyOfVisitedOrders, deepCopyOfCurrentMakespan, nextLateness, resultOrders, minLateness);
                int currentMaxLateness = recResult.getMaxLateness();
                deepCopyOfVisitedOrders = recResult.getResultSubgraph();
                if (currentMaxLateness > deepCopyOfMaxLateness) {
                    deepCopyOfMaxLateness = currentMaxLateness;
                }
                if(minLateness > deepCopyOfMaxLateness) {
                    minLateness = deepCopyOfMaxLateness;
                    resultOrders = deepCopyOfVisitedOrders;
                }
            }
        }

        return new ResultSubgraph(resultOrders, minLateness, currentMachine);
    }

    private int getCurrentMakespan(String currentMachine, int startingOrderId) { // todo for last of machines
        return getReleaseTimeFor2(currentMachine, startingOrderId) + getProductionTimeFor2(currentMachine, startingOrderId);
    }


    public void setString(String stringGraph) {
        this.stringGraph = stringGraph;
    }

    @Override
    public String toString() {
        return stringGraph;
    }
}
