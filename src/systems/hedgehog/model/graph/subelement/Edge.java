package systems.hedgehog.model.graph.subelement;

public class Edge {

    private Node firstNode;
    private Node secondNode;
    private Integer weight;

    public Node getFirstNode() {
        return firstNode;
    }

    public Node getSecondNode() {
        return secondNode;
    }

    public Integer getWeight() {
        return weight;
    }

    public Edge(Node firstNode, Node secondNode, Integer weight) {
        this.firstNode = firstNode;
        this.secondNode = secondNode;
        this.weight = weight;
    }

    @Override
    public boolean equals(Object anotherEdge) {
        if(anotherEdge instanceof Edge) {
            return firstNode.equals(((Edge) anotherEdge).getFirstNode())
                    && secondNode.equals(((Edge) anotherEdge).getSecondNode())
                    && weight.equals(((Edge) anotherEdge).getWeight());
        }
        return false;
    }
}
