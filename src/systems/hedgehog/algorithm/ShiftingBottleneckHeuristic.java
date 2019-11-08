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
                List<OrderInGraph> orders = currentBestRes.get().getResultSubgraph();
                for(int indexOfOrder = 0; indexOfOrder < orders.size() - 1; indexOfOrder++) {
                    Optional<Edge> currentEdge = graph.getLastEdgeFor(currentBestRes.get().getMachine(), orders.get(indexOfOrder).getOrderId());
                    Optional<Edge> nextEdge = graph.getLastEdgeFor(currentBestRes.get().getMachine(), orders.get(indexOfOrder + 1).getOrderId());
                    if(currentEdge.isPresent() && nextEdge.isPresent()) {
                        result.add(graph.addEdge(graph.getOrder(currentEdge.get()).get().getOrderId(), currentEdge.get().getFirstNode(), nextEdge.get().getFirstNode()));
                    }
                }
                machines.remove(currentBestRes.get().getMachine());
            } else {
                System.out.println("Whole makespan: " + graph.getMakespan());
                System.out.println("Added edges:" + result);
                break;
            }
            iteration++;
            System.out.println();
        }

        List<MakespanResult> mrList = new LinkedList<>();
        for(OrderInGraph order : graph.getOrders()) {
            mrList.add(graph.getMakespanForEndingOrder(order.getOrderId()));
        }

        System.out.println(mrList);

        return null;


    }


}
