package systems.hedgehog.algorithm;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.graph.subelement.ResultSubgraph;
import systems.hedgehog.model.result.SchedulingResult;
import systems.hedgehog.model.struct.Order;

import java.util.*;

public class ShiftingBottleneckHeuristic implements Algorithm {

    @Override
    public List<SchedulingResult> findScheduling(Graph graph) {

        Set<String> machines = graph.getMachines();
        int iteration = 1;
        Set<Edge> result = new LinkedHashSet<>();

        while (true) {
            List<ResultSubgraph> resultInIteration = new ArrayList<>();
            for(String machine : machines) {
                System.out.println(machine + " Iteration: " + iteration + ", Makespan: " + graph.getMakespan());
                for(Order order : graph.getOrdersWithMachine(machine)) {
                    System.out.println("Order: " + order.getOrderId() + ", Production time: " + graph.getProductionTimeFor(machine, order.getOrderId())
                            + ", R-Production time: " + graph.getProductionTimeIncludingBlockingFor(machine, order.getOrderId())
                            + ", Release: " + graph.getReleaseTimeFor(machine, order.getOrderId())
                            + ", R-Release: " + graph.getReleaseTimeIncludingBlockingFor(machine, order.getOrderId())
                            + ", Due Date: " + graph.getDueDateFor(machine, order.getOrderId()));
                }
                ResultSubgraph resultSubgraph = graph.getMinimizedMaxLatenessResultFor(machine);
                resultInIteration.add(resultSubgraph);
                System.out.println("Minimized Max Lateness Subgraph: " + resultSubgraph);
            }
            Optional<ResultSubgraph> currentMaxResult = resultInIteration.stream().max(Comparator.comparing(ResultSubgraph::getMaxLateness));
            if(currentMaxResult.isPresent()) {
                System.out.println("Chosen: " + currentMaxResult.get() + "\n");
                List<Edge> newEdges = currentMaxResult.get().getResultSubgraph();
                for(int indexOfEdge = 0; indexOfEdge < newEdges.size() - 1; indexOfEdge++) {
                    Edge currentEdge = newEdges.get(indexOfEdge);
                    Edge nextEdge = newEdges.get(indexOfEdge + 1);
                    if(graph.getOrder(currentEdge).isPresent() && graph.getOrder(nextEdge).isPresent()
                            && !graph.getOrder(currentEdge).get().equals(graph.getOrder(nextEdge).get())) {
                        result.add(graph.addEdge(graph.getOrder(currentEdge).get().getOrderId(), currentEdge.getFirstNode(), nextEdge.getFirstNode()));
                    } else {
                        result.add(currentEdge);
                    }

                }
                result.add(newEdges.get(newEdges.size() - 1));
                machines.remove(currentMaxResult.get().getMachine());
            } else {
                System.out.println("Whole makespan: " + graph.getMakespan());
                break;
            }
            iteration++;
        }

        List<SchedulingResult> schedulingResults = new LinkedList<>();
        for(Edge resultEdge : result) {
            int releaseTime = graph.getReleaseTimeForEdge(resultEdge);
            int endTime = releaseTime + resultEdge.getWeight();
            schedulingResults.add(new SchedulingResult(resultEdge.getFirstNode().getName(), resultEdge.getFirstNode().getMachine(), releaseTime, endTime));
        }
        return schedulingResults;
    }


}
