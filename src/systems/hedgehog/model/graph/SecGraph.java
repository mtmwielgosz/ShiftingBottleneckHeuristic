package systems.hedgehog.model.graph;

import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.graph.subelement.Node;
import systems.hedgehog.model.graph.subelement.Subgraph;
import systems.hedgehog.model.struct.OrderInGraph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SecGraph extends Graph {

    public SecGraph(Graph graph) {
        super();
        super.setOrders(graph.orders);
        super.setEdges(graph.edges);
        super.setString(graph.stringGraph);
    }





}
