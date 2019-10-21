package systems.hedgehog.factory;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.graph.subelement.Node;
import systems.hedgehog.model.struct.Order;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GraphFactory {

    public static Graph generateGraph(String srcFile) throws IOException {

        List<Node> allNodes = new ArrayList<>();

        List<String> source = Files.readAllLines(Paths.get(srcFile), Charset.forName("UTF-8"));
        int numberOfOrders = Integer.valueOf(source.get(0));
        List<Order> allOrders = new ArrayList<>();
        for(int index = 1; index <= numberOfOrders; index++) {
            List<String> jobsInCurrentOrder = new LinkedList<>(Arrays.asList(source.get(index).split(" ")));
            allOrders.add(new Order(jobsInCurrentOrder));
        }

        for(int index = numberOfOrders + 1; index < source.size(); index++) {
            String[] currentNode = source.get(index).split(" ");
            allNodes.add(new Node(currentNode[0], currentNode[2], Integer.valueOf(currentNode[1])));
        }

        Graph graph = new Graph();
        for(Order currentOrder : allOrders) {
            Node currentNode = allNodes.stream().filter(node -> node.getName().equals(currentOrder.getAllOrders().get(0))).findFirst().orElse(null);
            graph.addEdge(Graph.startNode, currentNode);

            Integer indexOfNextNode = 1;
            final Integer finalIndexOfFirstNextNode = indexOfNextNode;
            Node nextNode = allNodes.stream().filter(node -> node.getName().equals(currentOrder.getAllOrders().get(finalIndexOfFirstNextNode))).findFirst().orElse(null);
            while(nextNode != null) {
                graph.addEdge(currentNode, nextNode);
                currentNode = nextNode;
                indexOfNextNode++;
                final Integer finalIndexOfNextNode = indexOfNextNode;
                if(finalIndexOfNextNode < currentOrder.getAllOrders().size()) {
                    nextNode = allNodes.stream().filter(node -> node.getName().equals(currentOrder.getAllOrders().get(finalIndexOfNextNode))).findFirst().orElse(null);
                } else {
                    nextNode = null;
                }
            }
            graph.addEdge(currentNode, Graph.endNode);

        }
        return graph;
    }

}
