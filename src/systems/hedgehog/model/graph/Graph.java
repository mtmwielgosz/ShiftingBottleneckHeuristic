package systems.hedgehog.model.graph;

import com.sun.org.apache.xpath.internal.operations.Or;
import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.graph.subelement.Node;
import systems.hedgehog.model.graph.subelement.Subgraph;
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
          //              .filter(edge -> getReleaseTimeFor(currentMachine, currentOrder.getOrderId()) >= currentMakespan)
                        .findFirst();
            }
        }
        return Optional.empty();
    }

    public int getMakespan() {
        int maxMakespan = 0;
        for(OrderInGraph currentOrder : orders) {
            int makespanForOrder = getCurrentMaxMakespan(currentOrder.getJobsInOrder());
            if(makespanForOrder > maxMakespan) {
                maxMakespan = makespanForOrder;
            }
        }
        return maxMakespan;
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

    private List<Edge> getMaxLatenessSubgraphFor(String currentMachine, int orderId, int currentMakespan, List<Edge> visitedEdges) {
        Optional<Edge> currentEdge = getNextEdgeFor(currentMachine, currentMakespan, visitedEdges, orderId);
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

    private int getCurrentMaxMakespan(List<Edge> currentLatenessSubgraph) {

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

    public int getMinimizedLatenessFor(String currentMachine, int orderId) {
        List<Subgraph> minimizedSubgraph = getReducedMaximumLatenessSubgraphsFor(currentMachine);
        return minimizedSubgraph.stream().filter(subgraph -> subgraph.getOrderId() == orderId)
                .flatMapToInt(subgraph -> IntStream.of(subgraph.getMaxMakespan())).sum() - getMakespan();

    }

    private void addSucceedingEdges(int endingOrderId, List<Edge> subGraph) {
        Optional<OrderInGraph> endingOrder = orders.stream().filter(order -> order.getOrderId() == endingOrderId).findFirst();
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
