package systems.hedgehog.algorithm;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.graph.subelement.Node;
import systems.hedgehog.model.graph.subelement.ResultSubgraph;
import systems.hedgehog.model.result.MakespanResult;
import systems.hedgehog.model.result.SchedulingResult;
import systems.hedgehog.model.struct.OrderInGraph;

import java.util.*;
import java.util.stream.Collectors;

public class ShiftingBottleneckHeuristic implements Algorithm {

    @Override
    public List<SchedulingResult> findScheduling(Graph graph) {

        Set<String> machines = graph.getOrders().stream().map(OrderInGraph::getJobsInOrder).flatMap(List::stream).map(Edge::getFirstNode).map(Node::getMachine).filter(machine -> !"".equals(machine.trim())).collect(Collectors.toSet());
        int iteration = 1;
        Set<Edge> result = new LinkedHashSet<>();
        while (true) {

            List<ResultSubgraph> resInIteration = new ArrayList<>();
            for(String machine : machines) {

                System.out.println(machine + " interation: " + iteration + ", makespan: " + graph.getMakespan());
                for(OrderInGraph order : graph.getOrdersWithMachine(machine)) {
                    System.out.println("Order:" + order.getOrderId() + ", Prod:" + graph.getProductionTimeFor2(machine, order.getOrderId())
                            + ", R-Prod" + graph.getRealProductionTimeFor(machine, order.getOrderId())
                            + ", Release:" + graph.getReleaseTimeFor2(machine, order.getOrderId())
                            + ", R-Release:" + graph.getRealReleaseTimeFor2(machine, order.getOrderId())
                            + ", DueDate:" + graph.getDueDateFor2(machine, order.getOrderId()));
                }

                ResultSubgraph resSub = graph.getRealMinimizedMaxLatenessFor(machine);
                resInIteration.add(resSub);
                System.out.println("Minimized Max Lateness: " + resSub);
            }
            Optional<ResultSubgraph> currentBestRes = resInIteration.stream().max(Comparator.comparing(ResultSubgraph::getMaxLateness));

            if(currentBestRes.isPresent()) {
                System.out.println("Chosen: " + currentBestRes.get());
                List<Edge> newEdges = currentBestRes.get().getResultSubgraph();
                for(int indexOfEdge = 0; indexOfEdge < newEdges.size() - 1; indexOfEdge++) {
                    Edge currentEdge = newEdges.get(indexOfEdge);
                    Edge nextEdge = newEdges.get(indexOfEdge + 1);
                    if(graph.getOrder(currentEdge).isPresent() && graph.getOrder(nextEdge).isPresent()
                            && !graph.getOrder(currentEdge).get().equals(graph.getOrder(nextEdge).isPresent())) {
                        result.add(graph.addEdge(graph.getOrder(currentEdge).get().getOrderId(), currentEdge.getFirstNode(), nextEdge.getFirstNode()));
                    } else {
                        result.add(currentEdge);
                    }

                }
                result.add(newEdges.get(newEdges.size() - 1));
                machines.remove(currentBestRes.get().getMachine());
            } else {
                System.out.println("Whole makespan: " + graph.getMakespan());
                System.out.println("Added edges:" + result);
                break;
            }
            iteration++;
            System.out.println();
        }

        for(Edge resultEdge : result) {
            int releaseTime = graph.getReleaseTimeForEdge(resultEdge);
            int endTime = releaseTime + resultEdge.getWeight();
            System.out.println(resultEdge.getFirstNode().getName() + " - " +resultEdge.getFirstNode().getMachine() + ", start: " + releaseTime + ", end: " + endTime);
        }

        return null;


    }


}
