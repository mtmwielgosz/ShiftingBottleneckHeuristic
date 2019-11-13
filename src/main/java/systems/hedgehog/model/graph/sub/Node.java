package systems.hedgehog.model.graph.sub;

public class Node {

    private final String name;
    private final String machine;
    private final Integer weightToNextNode;
    private final Double agingRation;

    public String getName() {
        return name;
    }

    public String getMachine() {
        return machine;
    }

    public Integer getWeightToNextNode() {
        return weightToNextNode;
    }

    public Double getAgingRation() {
        return agingRation;
    }

    public Node(String name, String machine, Integer weightToNextNode, Double agingRation) {
        this.name = name;
        this.machine = machine;
        this.weightToNextNode = weightToNextNode;
        this.agingRation = agingRation;
    }

    @Override
    public boolean equals(Object anotherNode) {
        if(anotherNode instanceof  Node) {
            return name.equals(((Node) anotherNode).getName()) && machine.equals(((Node) anotherNode).getMachine());
        }
        return false;
    }
    
}
