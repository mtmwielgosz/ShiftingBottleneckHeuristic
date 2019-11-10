package systems.hedgehog.model.graph;

import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.graph.subelement.Node;
import systems.hedgehog.model.graph.subelement.ResultSubgraph;
import systems.hedgehog.model.result.MakespanResult;
import systems.hedgehog.model.struct.Order;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    private final Set<Edge> edges = new LinkedHashSet<>();
    private final Set<Order> orders = new LinkedHashSet<>();
    private String startingStringGraph;

    public static final Node startNode;
    public static final Node endNode;
    static {
        startNode = new Node("U", "", 0);
        endNode = new Node("V", "", 0);
    }

    private Optional<Edge> getLastEdgeFor(String currentMachine, int orderId) {
        List<Edge> currentEdges = edges.stream().filter(edge -> currentMachine.equals(edge.getFirstNode().getMachine()))
                .filter(edge -> getOrder(edge).isPresent() && getOrder(edge).get().getOrderId() == orderId).collect(Collectors.toList());
        if(currentEdges.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(currentEdges.get(currentEdges.size() - 1));
    }

    private Optional<Edge> getStartEdgeFor(int orderId) {
        Optional<Order> orderWithId = getOrder(orderId);
        if(orderWithId.isPresent()) {
            return edges.stream().filter(edge -> orderWithId.get().getJobsInOrder().contains(edge))
                    .filter(edge -> edge.getFirstNode().equals(Graph.startNode)).findFirst();
        }
        throw new IllegalArgumentException("Order with id " + orderId + " not found.");
    }

    private Optional<Edge> getFirstEdgeFor(String machine, int orderId) {
        Optional<Order> orderWithId = getOrder(orderId);
        if(orderWithId.isPresent()) {
            return edges.stream().filter(edge -> orderWithId.get().getJobsInOrder().contains(edge))
                    .filter(edge -> edge.getFirstNode().getMachine().equals(machine)).findFirst();
        }
        throw new IllegalArgumentException("Order with id " + orderId + " not found.");
    }

    private Set<Edge> getNextEdgesFor(Edge currentEdge) {
        if(currentEdge.getSecondNode().equals(endNode)) {
            return new LinkedHashSet<>();
        }
        return edges.stream().filter(edge -> currentEdge.getSecondNode().equals(edge.getFirstNode())).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> getMachines() {
        return orders.stream().map(Order::getJobsInOrder)
                .flatMap(List::stream).map(Edge::getFirstNode)
                .map(Node::getMachine).filter(machine -> !"".equals(machine.trim())).collect(Collectors.toSet());
    }

    public int getMakespan() {
        int maxMakespan = 0;
        for(Order order : orders) {
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
        if(endNode.equals(edge.getFirstNode())) {
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

    private MakespanResult getMakespanForEndingOrder(int endingOrderId) {
        int maxMakespan = 0;
        List<Edge> resultEdges = new LinkedList<>();
        for(Order startingOrder : orders) {
           Optional<Edge> startingEdge = getStartEdgeFor(startingOrder.getOrderId());
           if (startingEdge.isPresent()) {
               Set<Edge> nextEdges = getNextEdgesFor(startingEdge.get());
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
        Set<Edge> nextEdges = getNextEdgesFor(edge);
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

            if(endNode.equals(nextEdge.getSecondNode()) && getOrder(nextEdge).isPresent() && getOrder(nextEdge).get().getOrderId() == endingOrderId) {
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

    public Edge addEdge(int orderId, Node srcNode, Node destNode) {
        Edge newEdge = new Edge(srcNode, destNode, srcNode.getWeightToNextNode());
        edges.add(newEdge);
        Optional<Order> currentOrder = getOrder(orderId);
        if(currentOrder.isPresent()) {
            currentOrder.get().addJob(newEdge);
        } else {
            Order newOrder = new Order(orderId);
            newOrder.addJob(newEdge);
            orders.add(newOrder);
        }
        return edges.stream().filter(edge -> edge.equals(newEdge)).findFirst().orElse(null);
    }

    private Optional<Order> getOrder(int orderId) {
        return orders.stream().filter(order -> order.getOrderId() == orderId).findFirst();
    }

    public Optional<Order> getOrder(Edge edge) {
        return orders.stream().filter(order -> order.getJobsInOrder().contains(edge)).findFirst();
    }

    public int getProductionTimeFor(String currentMachine, int orderId) {
        Optional<Edge> lastEdge = getLastEdgeFor(currentMachine, orderId);
        if(lastEdge.isPresent()) {
            return lastEdge.get().getWeight();
        }
        return 0;
    }

    public int getProductionTimeIncludingBlockingFor(String currentMachine, int orderId) {
        int maxProductionTime =  getProductionTimeFor(currentMachine, orderId);
        int releaseTimeCurrent =  getReleaseTimeIncludingBlockingFor(currentMachine, orderId);

        Optional<Edge> currentEdge =  getLastEdgeFor(currentMachine, orderId);
        if(currentEdge.isPresent()) {
            Set<Edge> nextEdges = getNextEdgesFor(currentEdge.get());
            for(Edge nextEdge : nextEdges) {
                int releaseTimeForNextEdge = getReleaseTimeIncludingBlockingFor(nextEdge.getFirstNode().getMachine(), orderId);
                if(releaseTimeForNextEdge > releaseTimeCurrent + maxProductionTime) {
                    maxProductionTime = releaseTimeForNextEdge - releaseTimeCurrent;
                }
            }
        }
        return maxProductionTime;
    }

    public int getReleaseTimeFor(String currentMachine, int endingOrderId) {
        int releaseTime = 0;
        for(Order order : orders) {
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
            Optional<Edge> neededEdge = getLastEdgeFor(currentMachine, endingOrderId);
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
        for(Order order : orders) {
            Optional<Edge> startingEdge = getStartEdgeFor(order.getOrderId());
            if(startingEdge.isPresent()) {
                Set<Edge> nextEdges = getNextEdgesFor(startingEdge.get());
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
        Set<Edge> nextEdges = getNextEdgesFor(edge);
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
        for(Order order : orders) {
            Optional<Edge> startingEdge = getStartEdgeFor(order.getOrderId());
            if(startingEdge.isPresent()) {
                Set<Edge> nextEdges = getNextEdgesFor(startingEdge.get());
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
        Set<Edge> nextEdges = getNextEdgesFor(edge);
        int maxCurrentReleaseTime = 0;
        int stableCurrentReleaseTime = currentReleaseTime;
        for(Edge nextEdge : nextEdges) {
            Optional<Edge> nextEdgeWithSameMachine = nextEdges.stream().filter(nEdge -> edge.getFirstNode().getMachine().equals(nEdge.getFirstNode().getMachine())).findFirst();
            if(nextEdgeWithSameMachine.isPresent() && !nextEdge.equals(nextEdgeWithSameMachine.get())) {
                currentReleaseTime += nextEdgeWithSameMachine.get().getWeight();
                stableCurrentReleaseTime += nextEdgeWithSameMachine.get().getWeight();
            }
            Optional<Edge> neededEdge = getLastEdgeFor(currentMachine, endingOrderId);
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

    public int getDueDateFor(String currentMachine, int endingOrderId) {

        return getMakespan() - getMakespanForEndingOrder(endingOrderId).getMakespan() + getProductionTimeIncludingBlockingFor(currentMachine, endingOrderId) + getReleaseTimeIncludingBlockingFor(currentMachine, endingOrderId);
    }

    private int getDueDateForEdge(Edge edge) {
        Optional<Order> orderOfEdge = getOrder(edge);
        if(orderOfEdge.isPresent()) {
            return getMakespan() - getMakespanForEndingOrder(orderOfEdge.get().getOrderId()).getMakespan()
                    + edge.getWeight() + getReleaseTimeForEdge(edge);
        }
        throw new IllegalArgumentException("Edge " + edge + " is not included in any order.");
    }

    public List<Order> getOrdersWithMachine(String currentMachine) {
        return orders.stream().filter(
                order -> order.getJobsInOrder().stream().anyMatch(job -> currentMachine.equals(job.getFirstNode().getMachine()))).collect(Collectors.toList());
    }

    public ResultSubgraph getMinimizedMaxLatenessResultFor(String currentMachine) {
        int minLateness = Integer.MAX_VALUE;
        ResultSubgraph resultSubgraph = null;
        List<Order> allOrdersForMachine = getOrdersWithMachine(currentMachine);
        for(Order order : allOrdersForMachine) {
            ResultSubgraph currentResult = getMaxLatenessFor(currentMachine, order.getOrderId());
            if(currentResult.getMaxLateness() < minLateness) {
                minLateness = currentResult.getMaxLateness();
                resultSubgraph = currentResult;
            }
        }
        return resultSubgraph;
    }

    private ResultSubgraph getMaxLatenessFor(String currentMachine, int startingOrderId) {
        int currentMakespan = getCurrentMakespan(currentMachine, startingOrderId);
        int maxLateness = currentMakespan - getDueDateFor(currentMachine, startingOrderId);
        List<Edge> visitedEdges = new LinkedList<>();
        Optional<Edge> firstEdge = getFirstEdgeFor(currentMachine, startingOrderId);
        firstEdge.ifPresent(visitedEdges::add);
        return getMaxLatenessRecFor(currentMachine, visitedEdges, currentMakespan, maxLateness, new LinkedList<>(visitedEdges), Integer.MAX_VALUE);
    }

    private ResultSubgraph getMaxLatenessRecFor(String currentMachine, List<Edge> visitedEdges, int currentMakespan, int maxLateness, List<Edge> resultEdges, int minLateness) {
        List<Edge> allEdgesWithMachine = getAllEdgesWithMachine(currentMachine);
        if(visitedEdges.containsAll(allEdgesWithMachine)) {
            return new ResultSubgraph(visitedEdges, maxLateness, currentMachine);
        }
        List<Edge> copyOfvisitedEdges = new LinkedList<>(visitedEdges);
        for(Edge nextEdge : allEdgesWithMachine) {
            List<Edge> deepCopyOfVisitedEdges = new LinkedList<>(copyOfvisitedEdges);
            int deepCopyOfCurrentMakespan = currentMakespan;
            int deepCopyOfMaxLateness = maxLateness;
            if (!deepCopyOfVisitedEdges.contains(nextEdge)) {
                deepCopyOfVisitedEdges.add(nextEdge);
                int nextReleaseTime = getReleaseTimeForEdge(nextEdge);
                if (deepCopyOfCurrentMakespan < nextReleaseTime) {
                    deepCopyOfCurrentMakespan = nextReleaseTime;
                }
                deepCopyOfCurrentMakespan += nextEdge.getWeight();
                int nextLateness = deepCopyOfCurrentMakespan - getDueDateForEdge(nextEdge);
                ResultSubgraph recResult = getMaxLatenessRecFor(currentMachine, deepCopyOfVisitedEdges, deepCopyOfCurrentMakespan, nextLateness, resultEdges, minLateness);
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
        return new ResultSubgraph(resultEdges, minLateness, currentMachine);
    }

    private List<Edge> getAllEdgesWithMachine(String currentMachine) {
        return edges.stream().filter(edge -> edge.getFirstNode().getMachine().equals(currentMachine)).collect(Collectors.toList());
    }

    private int getCurrentMakespan(String currentMachine, int startingOrderId) {
        return getReleaseTimeIncludingBlockingFor(currentMachine, startingOrderId) + getProductionTimeFor(currentMachine, startingOrderId);
    }


    public void setString(String stringGraph) {
        this.startingStringGraph = stringGraph;
    }

    @Override
    public String toString() {
        return startingStringGraph;
    }
}
