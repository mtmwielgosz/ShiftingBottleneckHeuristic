package systems.hedgehog.model.graph;

import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.graph.subelement.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Graph {
    // no disjunctive arcs - żadnych rozłącznych łuków
    // conjunctive arcs - łuki łączące
    // node - wierzchołek
    // edge - krawędź

    private Set<Edge> edges = new HashSet<>();

    public static Node startNode;
    public static Node endNode;
    static {
        startNode = new Node("U", "", 0);
        endNode = new Node("V", "", 0);
    }

    public Edge addEdge(Node srcNode, Node destNode) {
        Edge newEdge = new Edge(srcNode, destNode, srcNode.getWeightToNextNode());
        edges.add(newEdge);
        return edges.stream().filter(edge -> edge.equals(newEdge)).findFirst().orElse(null);
    }
}
