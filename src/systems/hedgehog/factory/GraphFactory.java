package systems.hedgehog.factory;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.subelement.Node;
import systems.hedgehog.model.struct.Order;

import java.io.IOException;
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
        List<Order> allOrders = new LinkedList<>();
        for(int index = 1; index <= numberOfOrders; index++) {
            List<String> jobsInCurrentOrder = new LinkedList<>(Arrays.asList(source.get(index).split(" ")));
            allOrders.add(new Order(index - 1, jobsInCurrentOrder));
        }

        for(int index = numberOfOrders + 1; index < source.size(); index++) {
            String[] currentNode = source.get(index).split(" ");
            allNodes.add(new Node(currentNode[0], currentNode[2], Integer.valueOf(currentNode[1])));
        }

        Graph graph = new Graph();
        String graphString = "";
        for(Order currentOrder : allOrders) {
            Node currentNode = allNodes.stream().filter(node -> node.getName().equals(currentOrder.getAllJobs().get(0))).findFirst().orElse(null);
            graph.addEdge(currentOrder.getOrderId(), Graph.startNode, currentNode);
            String orderString = "(" + Graph.startNode.getName() + ") =" + Graph.startNode.getWeightToNextNode() + "=> ";

            Integer indexOfNextNode = 1;
            final Integer indexOfFirstNodeInOrder = indexOfNextNode;
            Node nextNode = allNodes.stream().filter(node -> node.getName().equals(currentOrder.getAllJobs().get(indexOfFirstNodeInOrder))).findFirst().orElse(null);
            while(nextNode != null) {
                graph.addEdge(currentOrder.getOrderId(), currentNode, nextNode);
                orderString += "(" + currentNode.getName() + ", " + currentNode.getMachine() + ") =" + currentNode.getWeightToNextNode() + "=> ";
                currentNode = nextNode;
                indexOfNextNode++;
                final Integer finalIndexOfNextNode = indexOfNextNode;
                if(finalIndexOfNextNode < currentOrder.getAllJobs().size()) {
                    nextNode = allNodes.stream().filter(node -> node.getName().equals(currentOrder.getAllJobs().get(finalIndexOfNextNode))).findFirst().orElse(null);
                } else {
                    nextNode = null;
                }
            }
            graph.addEdge(currentOrder.getOrderId(), currentNode, Graph.endNode);
            orderString += "(" + currentNode.getName() + ", " + currentNode.getMachine() + ") =" + currentNode.getWeightToNextNode() + "=> "
                    + "(" + Graph.endNode.getName() + ")";
            graphString += orderString + "\n";
        }

        graph.setString(graphString);
        return graph;
    }

}
