package systems.hedgehog.model.graph.subelement;

public class Node {

    private String name;
    private String machine;

    private Integer weightToNextNode;

    public String getName() {
        return name;
    }

    public String getMachine() {
        return machine;
    }

    public Integer getWeightToNextNode() {
        return weightToNextNode;
    }

    public Node(String name, String machine, Integer weightToNextNode) {
        this.name = name;
        this.machine = machine;
        this.weightToNextNode = weightToNextNode;
    }

    @Override
    public boolean equals(Object anotherNode) {
        if(anotherNode instanceof  Node) {
            return name.equals(((Node) anotherNode).getName()) && machine.equals(((Node) anotherNode).getMachine());
        }
        return false;
    }
}
