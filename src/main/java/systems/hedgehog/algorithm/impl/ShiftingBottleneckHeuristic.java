package systems.hedgehog.algorithm.impl;

import systems.hedgehog.algorithm.Algorithm;
import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.sub.Edge;
import systems.hedgehog.model.result.SubgraphResult;
import systems.hedgehog.model.result.SchedulingResult;
import systems.hedgehog.model.graph.sub.Order;

import java.util.*;

public class ShiftingBottleneckHeuristic implements Algorithm {

    @Override
    public List<SchedulingResult> findScheduling(Graph graph) {

        Set<String> machines = graph.getMachines();
        Set<Edge> result = new LinkedHashSet<>();
        while (true) {
            List<SubgraphResult> resultInIteration = new ArrayList<>();
            for(String machine : machines) {
                SubgraphResult subgraphResult = graph.getMinimizedMaxLatenessResultFor(machine);
                resultInIteration.add(subgraphResult);
            }
            Optional<SubgraphResult> currentMaxResult = resultInIteration.stream().max(Comparator.comparing(SubgraphResult::getMaxLateness));
            if(currentMaxResult.isPresent()) {
                result.addAll(addNewEdgesToGraph(graph, machines, currentMaxResult.get()));
            } else {
                return getSchedulingResults(graph, result);
            }
        }
    }

    @Override
    public List<SchedulingResult> findSchedulingWithConsoleLogs(Graph graph) {

        Set<String> machines = graph.getMachines();
        int iteration = 1;
        Set<Edge> result = new LinkedHashSet<>();

        while (true) {
            List<SubgraphResult> resultInIteration = new ArrayList<>();
            for(String machine : machines) {
                System.out.println(machine + " Iteration: " + iteration + ", Makespan: " + graph.getMakespan());
                for(Order order : graph.getOrdersWithMachine(machine)) {
                    System.out.println("Order: " + order.getOrderId() + ", Production time: " + graph.getProductionTimeFor(machine, order.getOrderId())
                            + ", R-Production time: " + graph.getProductionTimeIncludingBlockingFor(machine, order.getOrderId())
                            + ", Release: " + graph.getReleaseTimeFor(machine, order.getOrderId())
                            + ", R-Release: " + graph.getReleaseTimeIncludingBlockingFor(machine, order.getOrderId())
                            + ", Due Date: " + graph.getDueDateFor(machine, order.getOrderId()));
                }
                SubgraphResult subgraphResult = graph.getMinimizedMaxLatenessResultFor(machine);
                resultInIteration.add(subgraphResult);
                System.out.println("Minimized Max Lateness Subgraph: " + subgraphResult);
            }
            Optional<SubgraphResult> currentMaxResult = resultInIteration.stream().max(Comparator.comparing(SubgraphResult::getMaxLateness));
            if(currentMaxResult.isPresent()) {
                System.out.println("Chosen: " + currentMaxResult.get() + "\n");
                result.addAll(addNewEdgesToGraph(graph, machines, currentMaxResult.get()));
            } else {
                System.out.println("Whole makespan: " + graph.getMakespan());
                break;
            }
            iteration++;
        }
        return getSchedulingResults(graph, result);
    }


    private List<SchedulingResult> getSchedulingResults(Graph graph, Set<Edge> result) {
        List<SchedulingResult> schedulingResults = new LinkedList<>();
        for(Edge resultEdge : result) {
            int releaseTime = graph.getReleaseTimeForEdge(resultEdge);
            int endTime = releaseTime + resultEdge.getWeight();
            schedulingResults.add(new SchedulingResult(resultEdge.getFirstNode().getName(), resultEdge.getFirstNode().getMachine(), releaseTime, endTime));
        }
        return schedulingResults;
    }

    private Set<Edge> addNewEdgesToGraph(Graph graph, Set<String> machines, SubgraphResult currentMaxResult) {
        Set<Edge> addedEdges = new LinkedHashSet<>();
        List<Edge> newEdges = currentMaxResult.getResultSubgraph();
        for(int indexOfEdge = 0; indexOfEdge < newEdges.size() - 1; indexOfEdge++) {
            Edge currentEdge = newEdges.get(indexOfEdge);
            Edge nextEdge = newEdges.get(indexOfEdge + 1);
            if(graph.getOrder(currentEdge).isPresent() && graph.getOrder(nextEdge).isPresent()
                    && !graph.getOrder(currentEdge).get().equals(graph.getOrder(nextEdge).get())) {
                addedEdges.add(graph.addEdge(graph.getOrder(currentEdge).get().getOrderId(), currentEdge.getFirstNode(), nextEdge.getFirstNode()));
            } else {
                addedEdges.add(currentEdge);
            }
        }
        addedEdges.add(newEdges.get(newEdges.size() - 1));
        machines.remove(currentMaxResult.getMachine());
        return addedEdges;
    }

}
