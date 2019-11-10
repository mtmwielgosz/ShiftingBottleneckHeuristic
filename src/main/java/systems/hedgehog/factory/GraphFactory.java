package systems.hedgehog.factory;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.sub.Node;
import systems.hedgehog.factory.sub.OrderInFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GraphFactory {

    public static Graph generateGraph(Path srcFile) throws IOException {

        List<Node> allNodes = new ArrayList<>();
        List<String> source = Files.readAllLines(srcFile, StandardCharsets.UTF_8);
        int numberOfOrders = Integer.parseInt(source.get(0));
        List<OrderInFile> allOrderInFile = new LinkedList<>();
        for(int index = 1; index <= numberOfOrders; index++) {
            List<String> jobsInCurrentOrder = new LinkedList<>(Arrays.asList(source.get(index).split(" ")));
            allOrderInFile.add(new OrderInFile(index - 1, jobsInCurrentOrder));
        }

        for(int index = numberOfOrders + 1; index < source.size(); index++) {
            String[] currentNode = source.get(index).split(" ");
            allNodes.add(new Node(currentNode[0], currentNode[2], Integer.valueOf(currentNode[1])));
        }

        Graph graph = new Graph();
        StringBuilder graphString = new StringBuilder();
        for(OrderInFile currentOrderInFile : allOrderInFile) {
            Node currentNode = allNodes.stream().filter(node -> node.getName().equals(currentOrderInFile.getAllJobs().get(0))).findFirst().orElse(null);
            graph.addEdge(currentOrderInFile.getOrderId(), Graph.startNode, currentNode);
            StringBuilder orderString = new StringBuilder("(" + Graph.startNode.getName() + ") =" + Graph.startNode.getWeightToNextNode() + "=> ");

            int indexOfNextNode = 1;
            final int indexOfFirstNodeInOrder = indexOfNextNode;
            Node nextNode = allNodes.stream().filter(node -> node.getName().equals(currentOrderInFile.getAllJobs().get(indexOfFirstNodeInOrder))).findFirst().orElse(null);
            while(nextNode != null) {
                graph.addEdge(currentOrderInFile.getOrderId(), currentNode, nextNode);
                assert currentNode != null;
                orderString.append("(").append(currentNode.getName()).append(", ").append(currentNode.getMachine()).append(") =").append(currentNode.getWeightToNextNode()).append("=> ");
                currentNode = nextNode;
                indexOfNextNode++;
                final int finalIndexOfNextNode = indexOfNextNode;
                if(finalIndexOfNextNode < currentOrderInFile.getAllJobs().size()) {
                    nextNode = allNodes.stream().filter(node -> node.getName().equals(currentOrderInFile.getAllJobs().get(finalIndexOfNextNode))).findFirst().orElse(null);
                } else {
                    nextNode = null;
                }
            }
            graph.addEdge(currentOrderInFile.getOrderId(), currentNode, Graph.endNode);
            assert currentNode != null;
            orderString.append("(").append(currentNode.getName()).append(", ").append(currentNode.getMachine()).append(") =").append(currentNode.getWeightToNextNode()).append("=> ").append("(").append(Graph.endNode.getName()).append(")");
            graphString.append(orderString).append("\n");
        }

        graph.setString(graphString.toString());
        return graph;
    }

}
