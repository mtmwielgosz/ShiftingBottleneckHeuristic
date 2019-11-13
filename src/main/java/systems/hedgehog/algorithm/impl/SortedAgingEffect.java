package systems.hedgehog.algorithm.impl;

import systems.hedgehog.algorithm.Algorithm;
import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.sub.Edge;
import systems.hedgehog.model.graph.sub.Order;
import systems.hedgehog.model.result.SchedulingResult;
import systems.hedgehog.model.result.SubgraphResult;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class SortedAgingEffect implements Algorithm {

    @Override
    public List<SchedulingResult> findScheduling(Graph graph) {

        Set<String> machines = graph.getMachines();
        Set<Edge> result = new LinkedHashSet<>();
        Set<String> sortedMadchines = machines.stream().sorted(Comparator.comparing(machine ->
                graph.getAllEdgesWithMachine(machine).stream().flatMapToDouble(
                        edge -> DoubleStream.of(edge.getAgingRation())).summaryStatistics().getAverage())).collect(Collectors.toCollection(LinkedHashSet::new));

        for(String machine : sortedMadchines) {
            List<Edge> newEdges = graph.getAllEdgesWithMachine(machine).stream().sorted(Comparator.comparing(Edge::getAgingRation)).collect(Collectors.toList());
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
        }

       return getSchedulingResults(graph, result);
    }

    @Override
    public List<SchedulingResult> findSchedulingWithConsoleLogs(Graph graph) {
        return findScheduling(graph);
    }

    private List<SchedulingResult> getSchedulingResults(Graph graph, Set<Edge> result) {
        List<SchedulingResult> schedulingResults = new LinkedList<>();
        int minReleaseTime = Integer.MAX_VALUE;
        for(Edge resultEdge : result) {
            int releaseTime = graph.getReleaseTimeForEdge(resultEdge);
            if(releaseTime < minReleaseTime) {
                minReleaseTime = releaseTime;
            }
        }

        for(Edge resultEdge : result) {
            int releaseTime = graph.getReleaseTimeForEdge(resultEdge) - minReleaseTime;
            int endTime = releaseTime + resultEdge.getWeight();
            schedulingResults.add(new SchedulingResult(resultEdge.getFirstNode().getName(), resultEdge.getFirstNode().getMachine(), releaseTime, endTime));
        }

        schedulingResults = schedulingResults.stream().sorted(Comparator.comparing(res -> res.getStartTime())).collect(Collectors.toList());
        return schedulingResults;
    }

}
