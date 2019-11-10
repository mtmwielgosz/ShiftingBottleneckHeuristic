package systems.hedgehog.model.graph;

import systems.hedgehog.model.graph.calc.*;
import systems.hedgehog.model.graph.sub.Edge;
import systems.hedgehog.model.graph.sub.Node;
import systems.hedgehog.model.result.SubgraphResult;
import systems.hedgehog.model.result.MakespanResult;
import systems.hedgehog.model.graph.sub.Order;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    private final Set<Edge> edges = new LinkedHashSet<>();
    private final Set<Order> orders = new LinkedHashSet<>();
    protected MakespanCalc makespanCalc;
    protected ProductionTimeCalc productionTimeCalc;
    protected ReleaseTimeCalc releaseTimeCalc;
    protected DueDateCalc dueDateCalc;
    protected MaxLatenessCalc maxLatenessCalc;
    private String startingStringGraph;

    public static final Node startNode;
    public static final Node endNode;
    static {
        startNode = new Node("U", "", 0);
        endNode = new Node("V", "", 0);
    }

    public Graph() {
        this.makespanCalc = new MakespanCalc(this);
        this.productionTimeCalc = new ProductionTimeCalc(this);
        this.releaseTimeCalc = new ReleaseTimeCalc(this);
        this.dueDateCalc = new DueDateCalc(this);
        this.maxLatenessCalc = new MaxLatenessCalc(this);
    }

    public Optional<Edge> getLastEdgeFor(String currentMachine, int orderId) {
        List<Edge> currentEdges = edges.stream().filter(edge -> currentMachine.equals(edge.getFirstNode().getMachine()))
                .filter(edge -> getOrder(edge).isPresent() && getOrder(edge).get().getOrderId() == orderId).collect(Collectors.toList());
        if(currentEdges.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(currentEdges.get(currentEdges.size() - 1));
    }

    public Optional<Edge> getStartEdgeFor(int orderId) {
        Optional<Order> orderWithId = getOrder(orderId);
        if(orderWithId.isPresent()) {
            return edges.stream().filter(edge -> orderWithId.get().getJobsInOrder().contains(edge))
                    .filter(edge -> edge.getFirstNode().equals(Graph.startNode)).findFirst();
        }
        throw new IllegalArgumentException("Order with id " + orderId + " not found.");
    }

    public Optional<Edge> getFirstEdgeFor(String machine, int orderId) {
        Optional<Order> orderWithId = getOrder(orderId);
        if(orderWithId.isPresent()) {
            return edges.stream().filter(edge -> orderWithId.get().getJobsInOrder().contains(edge))
                    .filter(edge -> edge.getFirstNode().getMachine().equals(machine)).findFirst();
        }
        throw new IllegalArgumentException("Order with id " + orderId + " not found.");
    }

    public Set<Edge> getNextEdgesFor(Edge currentEdge) {
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

    public Set<Order> getOrders() {
        return orders;
    }

    private Optional<Order> getOrder(int orderId) {
        return orders.stream().filter(order -> order.getOrderId() == orderId).findFirst();
    }

    public Optional<Order> getOrder(Edge edge) {
        return orders.stream().filter(order -> order.getJobsInOrder().contains(edge)).findFirst();
    }

    public List<Order> getOrdersWithMachine(String currentMachine) {
        return orders.stream().filter(
                order -> order.getJobsInOrder().stream().anyMatch(job -> currentMachine.equals(job.getFirstNode().getMachine()))).collect(Collectors.toList());
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

    public int getMakespan() {
        return makespanCalc.getMakespan();
    }

    public int getCurrentMakespan(String currentMachine, int startingOrderId) {
        return makespanCalc.getCurrentMakespan(currentMachine, startingOrderId);
    }

    public MakespanResult getMakespanForEndingOrder(int endingOrderId) {
        return makespanCalc.getMakespanForEndingOrder(endingOrderId);
    }

    public int getProductionTimeFor(String currentMachine, int orderId) {
        return productionTimeCalc.getProductionTimeFor(currentMachine, orderId);
    }

    public int getProductionTimeIncludingBlockingFor(String currentMachine, int orderId) {
        return productionTimeCalc.getProductionTimeIncludingBlockingFor(currentMachine, orderId);
    }

    public int getReleaseTimeFor(String currentMachine, int endingOrderId) {
        return releaseTimeCalc.getReleaseTimeFor(currentMachine, endingOrderId);
    }

    public int getReleaseTimeForEdge(Edge edge) {
        return releaseTimeCalc.getReleaseTimeForEdge(edge);
    }

    public int getReleaseTimeIncludingBlockingFor(String currentMachine, int endingOrderId) {
        return releaseTimeCalc.getReleaseTimeIncludingBlockingFor(currentMachine, endingOrderId);
    }

    public int getDueDateFor(String currentMachine, int endingOrderId) {
        return dueDateCalc.getDueDateFor(currentMachine, endingOrderId);
    }

    public int getDueDateForEdge(Edge edge) {
        return dueDateCalc.getDueDateForEdge(edge);
    }

    public SubgraphResult getMinimizedMaxLatenessResultFor(String currentMachine) {
        return maxLatenessCalc.getMinimizedMaxLatenessResultFor(currentMachine);
    }

    public List<Edge> getAllEdgesWithMachine(String currentMachine) {
        return edges.stream().filter(edge -> edge.getFirstNode().getMachine().equals(currentMachine)).collect(Collectors.toList());
    }

    public void setString(String stringGraph) {
        this.startingStringGraph = stringGraph;
    }

    @Override
    public String toString() {
        return startingStringGraph;
    }
}
