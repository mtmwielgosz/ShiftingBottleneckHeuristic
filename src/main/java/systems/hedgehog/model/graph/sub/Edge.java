package systems.hedgehog.model.graph.sub;

import java.util.Objects;

public class Edge {

    @SuppressWarnings("CanBeFinal")
    private final Node firstNode;
    private final Node secondNode;
    private final Integer weight;
    private final Double agingRation;

    public Node getFirstNode() {
        return firstNode;
    }

    public Node getSecondNode() {
        return secondNode;
    }

    public Integer getWeight() {
        return weight;
    }

    public Double getAgingRation() {
        return agingRation;
    }

    public Edge(Node firstNode, Node secondNode, Integer weight, Double agingRation) {
        this.firstNode = firstNode;
        this.secondNode = secondNode;
        this.weight = weight;
        this.agingRation = agingRation;
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

    @Override
    public int hashCode() {
        return Objects.hash(firstNode, weight, secondNode);
    }

    @Override
    public String toString () {
        return "(" + firstNode.getName() + "," + firstNode.getMachine() + ") =" + weight + "=>" + "(" + secondNode.getName() + "," + secondNode.getMachine() + ")";
    }
}
