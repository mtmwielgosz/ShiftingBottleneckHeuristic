package systems.hedgehog.model.graph;

import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.graph.subelement.Node;
import systems.hedgehog.model.struct.OrderInGraph;

import java.util.*;
import java.util.stream.IntStream;

public class Graph {

    private Set<Edge> edges = new HashSet<>();
    private List<OrderInGraph> orders = new ArrayList<>();
    private String stringGraph;

    public static Node startNode;
    public static Node endNode;
    static {
        startNode = new Node("U", "", 0);
        endNode = new Node("V", "", 0);
    }

    public List<OrderInGraph> getOrders() {
        return orders;
    }

    public Optional<Edge> getStartEdgeFor(int orderId) {
        return edges.stream().filter(edge -> orders.get(orderId).getJobsInOrder().contains(edge))
                .filter(edge -> edge.getFirstNode().equals(Graph.startNode)).findFirst();
    }

    public Optional<Edge> getNextEdgeFor(Edge currentEdge, int orderId) {
        if(currentEdge.getSecondNode().equals(Graph.endNode)) {
            return Optional.empty();
        }
        return edges.stream().filter(edge -> orders.get(orderId).getJobsInOrder().contains(edge))
                .filter(edge -> currentEdge.getSecondNode().equals(edge.getFirstNode())).findFirst();
    }

    public Optional<Edge> getNextEdgeFor(String currentMachine, int currentMakespan, List<Edge> notInEdge, int notOrderId) {
        for(OrderInGraph currentOrder : orders) {
            if(currentOrder.getOrderId() != notOrderId) {
                return edges.stream().filter(edge -> currentMachine.equals(edge.getFirstNode().getMachine()))
                        .filter(edge -> !notInEdge.contains(edge))
                        .filter(edge -> getReleaseTimeFor(currentMachine, currentOrder.getOrderId()) <= currentMakespan)
                        .findFirst();
            }
        }
        return Optional.empty();
    }

    public int getMakespan() {
        int maxMakespan = 0;
        for(OrderInGraph currentOrder : orders) {
            int makespanForOrder = currentOrder.getJobsInOrder().stream().flatMapToInt(order -> IntStream.of(order.getWeight())).sum();
            if(makespanForOrder > maxMakespan) {
                maxMakespan = makespanForOrder;
            }
        }
        return  maxMakespan;
    }

    public Edge addEdge(int orderId, Node srcNode, Node destNode) {
        Edge newEdge = new Edge(srcNode, destNode, srcNode.getWeightToNextNode());
        edges.add(newEdge);
        Optional<OrderInGraph> currentOrder = orders.stream().filter(order -> order.getOrderId() == orderId).findFirst();
        if(currentOrder.isPresent()) {
            currentOrder.get().addJob(newEdge);
        } else {
            OrderInGraph newOrderInGraph = new OrderInGraph(orderId);
            newOrderInGraph.addJob(newEdge);
            orders.add(newOrderInGraph);
        }
        return edges.stream().filter(edge -> edge.equals(newEdge)).findFirst().orElse(null);
    }

    public int getProductionTimeFor(String currentMachine, int orderId) {
        return edges.stream().filter(edge -> orders.get(orderId).getJobsInOrder().contains(edge))
                .filter(edge -> currentMachine.equals(edge.getFirstNode().getMachine()))
                .flatMapToInt(edge -> IntStream.of(edge.getWeight())).sum();
    }

    public int getReleaseTimeFor(String currentMachine, int orderId) {
        Optional<Edge> currentEdge = getStartEdgeFor(orderId);
        int releaseTime = 0;
        while(currentEdge.isPresent() && !currentMachine.equals(currentEdge.get().getFirstNode().getMachine())) {
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
        currentEdge = getNextEdgeFor(currentEdge.get(), orderId);

        int succeedingProcessingTime = 0;
        while(currentEdge.isPresent()) {
            succeedingProcessingTime += currentEdge.get().getWeight();
            currentEdge = getNextEdgeFor(currentEdge.get(), orderId);
        }

        return getMakespan() - succeedingProcessingTime;
    }

    private List<Edge> getMaxLatenessSubgraphFor(String currentMachine, int orderId, int currentMakespan, List<Edge> visitedEdges) {
        Optional<Edge> currentEdge = getNextEdgeFor(currentMachine, currentMakespan, visitedEdges, orderId);
        if(currentEdge.isPresent()) {
            visitedEdges.add(currentEdge.get());
            currentMakespan += currentEdge.get().getWeight();
            visitedEdges = getMaxLatenessSubgraphFor(currentMachine, orderId, currentMakespan, visitedEdges);
        }
        return visitedEdges;
    }

    public List<Edge> getMaxLatenessEdgesFor(String currentMachine, int orderId, int currentMakespan, List<Edge> visitedEdges) {
        Optional<Edge> currentEdge = getStartEdgeFor(orderId);
        currentEdge = getNextEdgeFor(currentEdge.get(), orderId);
        if(currentMachine.equals(currentEdge.get().getFirstNode().getMachine())) {
            visitedEdges.add(currentEdge.get());
            currentMakespan += currentEdge.get().getWeight();
            List<Edge> subGraph = getMaxLatenessSubgraphFor(currentMachine, orderId, currentMakespan, visitedEdges);
            Edge lastEdge = subGraph.get(subGraph.size() - 1);
            Optional<OrderInGraph> currentOrder = orders.stream().filter(order -> order.getJobsInOrder().contains(lastEdge)).findFirst();
            if (currentOrder.isPresent()) {
                Optional<Edge> nextEdge = getNextEdgeFor(lastEdge, currentOrder.get().getOrderId());
                while (nextEdge.isPresent()) {
                    subGraph.add(nextEdge.get());
                    nextEdge = getNextEdgeFor(nextEdge.get(), currentOrder.get().getOrderId());
                }
            }
            return subGraph;
        }
        return new LinkedList<>();
    }

    public List<Edge> getMaxLatenessEdgesForEndingOrder(String currentMachine, int endingOrderId, int currentMakespan, List<Edge> visitedEdges) {
        int maxLateness = 0;
        for(OrderInGraph currentOrder : orders) {
            if (currentOrder.getOrderId() != endingOrderId) {
                Optional<Edge> currentEdge = getStartEdgeFor(currentOrder.getOrderId());
                currentEdge = getNextEdgeFor(currentEdge.get(), currentOrder.getOrderId());
                if (currentMachine.equals(currentEdge.get().getFirstNode().getMachine())) {
                    visitedEdges.add(currentEdge.get());
                    currentMakespan += currentEdge.get().getWeight();
                    List<Edge> subGraph = getMaxLatenessSubgraphFor(currentMachine, currentOrder.getOrderId(), currentMakespan, visitedEdges);
                    Optional<OrderInGraph> endingOrder = orders.stream().filter(order -> order.getOrderId() == endingOrderId).findFirst();
                    if (endingOrder.isPresent()) {
                        Optional<Edge> lastEdge = subGraph.stream().filter(edge -> endingOrder.get().getJobsInOrder().contains(edge)).findFirst();
                        if (lastEdge.isPresent()) {
                            for (int index = subGraph.indexOf(lastEdge.get()) + 1; index < subGraph.size(); index++) {
                                subGraph.remove(index);
                            }
                            Optional<Edge> nextEdge = getNextEdgeFor(lastEdge.get(), endingOrder.get().getOrderId());
                            while (nextEdge.isPresent()) {
                                subGraph.add(nextEdge.get());
                                nextEdge = getNextEdgeFor(nextEdge.get(), endingOrder.get().getOrderId());
                            }
                        }
                    }
                    return subGraph;
                }
            }
        }
        return new LinkedList<>();
    }

    public void setString(String stringGraph) {
        this.stringGraph = stringGraph;
    }

    @Override
    public String toString() {
        return stringGraph;
    }
}
