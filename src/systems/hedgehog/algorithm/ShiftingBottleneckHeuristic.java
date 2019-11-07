package systems.hedgehog.algorithm;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.graph.subelement.Node;
import systems.hedgehog.model.graph.subelement.ResultSubgraph;
import systems.hedgehog.model.graph.subelement.Subgraph;
import systems.hedgehog.model.result.SchedulingResult;
import systems.hedgehog.model.struct.OrderInGraph;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

public class ShiftingBottleneckHeuristic implements Algorithm {

    @Override
    public List<SchedulingResult> findScheduling(Graph graph) {

        Set<String> machines = graph.getOrders().stream().map(OrderInGraph::getJobsInOrder).flatMap(List::stream).map(Edge::getFirstNode).map(Node::getMachine).filter(machine -> !"".equals(machine.trim())).collect(Collectors.toSet());
        while (true) {

            List<ResultSubgraph> resInIteration = new ArrayList<>();
            for(String machine : machines) {
                resInIteration.add(graph.getRealMinimizedMaxLatenessFor(machine));
            }
            Optional<ResultSubgraph> currentBestRes = resInIteration.stream().max(Comparator.comparing(ResultSubgraph::getMaxLateness));

            if(currentBestRes.isPresent()) {
                System.out.println(currentBestRes.get());
                List<OrderInGraph> orders1 = currentBestRes.get().getResultSubgraph();
                for(int indexOfOrder = 0; indexOfOrder < orders1.size() - 1; indexOfOrder++) {
                    Optional<Edge> currentEdge = graph.getEdgeFor(currentBestRes.get().getMachine(), orders1.get(indexOfOrder).getOrderId());
                    Optional<Edge> nextEdge = graph.getEdgeFor(currentBestRes.get().getMachine(), orders1.get(indexOfOrder + 1).getOrderId());
                    if(currentEdge.isPresent() && nextEdge.isPresent()) {
                        graph.addEdge(graph.getOrder(currentEdge.get()).get().getOrderId(), currentEdge.get().getFirstNode(), nextEdge.get().getFirstNode());
                    }
                }
                machines.remove(currentBestRes.get().getMachine());
            } else {
                System.out.println("Makespan: " + graph.getMakespan());
                break;
            }

        }



        /*
        System.out.println("M1");
        System.out.println(graph.getMakespan());
        System.out.println(graph.getProductionTimeFor2("M1", 0) + " " + graph.getReleaseTimeFor2("M1", 0)
                + " " + graph.getDueDateFor2("M1", 0)
                + " " + graph.getMinimizedLatenessFor("M1", 0)
                + "R-lateness: " + graph.getRealMaxLatenessFor("M1", 0));
        System.out.println(graph.getProductionTimeFor2("M1", 1) + " " + graph.getReleaseTimeFor2("M1", 1)
                + " " + graph.getDueDateFor2("M1", 1)
                + " " + graph.getMinimizedLatenessFor("M1", 1)
                + "R-lateness: " + graph.getRealMaxLatenessFor("M1", 1));
        System.out.println(graph.getProductionTimeFor2("M1", 2) + " " + graph.getReleaseTimeFor2("M1", 2)
                + " " + graph.getDueDateFor2("M1", 2)
                + " " + graph.getMinimizedLatenessFor("M1", 2)
                + "R-lateness: " + graph.getRealMaxLatenessFor("M1", 2));

        System.out.println("MinimizedLateness for M1 -> " + graph.getRealMinimizedMaxLatenessFor("M1"));

        System.out.println("M2");
        System.out.println(graph.getProductionTimeFor2("M2", 0) + " " + graph.getReleaseTimeFor2("M2", 0)
                + " " + graph.getDueDateFor2("M2", 0)
                + " " + graph.getMinimizedLatenessFor("M2", 0));
        System.out.println(graph.getProductionTimeFor2("M2", 1) + " " + graph.getReleaseTimeFor2("M2", 1)
                + " " + graph.getDueDateFor2("M2", 1)
                + " " + graph.getMinimizedLatenessFor("M2", 1));
        System.out.println(graph.getProductionTimeFor2("M2", 2) + " " + graph.getReleaseTimeFor2("M2", 2)
                + " " + graph.getDueDateFor2("M2", 2)
                + " " + graph.getMinimizedLatenessFor("M2", 2));

        System.out.println("MinimizedLateness for M2 -> " + graph.getRealMinimizedMaxLatenessFor("M2"));

        System.out.println("M3");
        System.out.println(graph.getProductionTimeFor2("M3", 0) + " " + graph.getReleaseTimeFor2("M3", 0)
                + " " + graph.getDueDateFor2("M3", 0)
                + " " + graph.getMinimizedLatenessFor("M3", 0));
        System.out.println(graph.getProductionTimeFor2("M3", 1) + " " + graph.getReleaseTimeFor2("M3", 1)
                + " " + graph.getDueDateFor2("M3", 1)
                + " " + graph.getMinimizedLatenessFor("M3", 1));

        System.out.println("MinimizedLateness for M3 -> " + graph.getRealMinimizedMaxLatenessFor("M3"));

        System.out.println("M4");
        System.out.println(graph.getProductionTimeFor2("M4", 1) + " " + graph.getReleaseTimeFor2("M4", 1)
                + " " + graph.getDueDateFor2("M4", 1)
                + " " + graph.getMinimizedLatenessFor("M4", 1));
        System.out.println(graph.getProductionTimeFor2("M4", 2) + " " + graph.getReleaseTimeFor2("M4", 2)
                + " " + graph.getDueDateFor2("M4", 2)
                + " " + graph.getMinimizedLatenessFor("M4", 2));

        System.out.println("MinimizedLateness for M4 -> " + graph.getRealMinimizedMaxLatenessFor("M4"));

        List<Edge> edges01 = graph.getLatenessSubgraph("M1", 0, 1);

        List<Edge> edges02 = graph.getLatenessSubgraph("M1", 0, 2);

        List<Edge> edges20 = graph.getLatenessSubgraph("M1", 2, 0);

        List<Edge> edges21 = graph.getLatenessSubgraph("M1", 2, 1);

        List<Edge> edges12 = graph.getLatenessSubgraph("M1", 1, 2);

        List<Edge> edges00 = graph.getLatenessSubgraph("M1", 0, 0);


        List<Subgraph> minimalizedSubgraphs1 = graph.getReducedMaximumLatenessSubgraphsFor("M1");

        List<Subgraph> minimalizedSubgraphs2 = graph.getReducedMaximumLatenessSubgraphsFor("M2");

        List<Subgraph> minimalizedSubgraphs3 = graph.getReducedMaximumLatenessSubgraphsFor("M3");

        List<Subgraph> minimalizedSubgraphs4 = graph.getReducedMaximumLatenessSubgraphsFor("M4");


        ResultSubgraph resSubFor1 = graph.getRealMinimizedMaxLatenessFor("M1");
        List<OrderInGraph> orders1 = resSubFor1.getResultSubgraph();
        for(int indexOfOrder = 0; indexOfOrder < orders1.size() - 1; indexOfOrder++) {
            Optional<Edge> currentEdge = graph.getEdgeFor("M1", indexOfOrder);
            Optional<Edge> nextEdge = graph.getEdgeFor("M1", indexOfOrder + 1);
            if(currentEdge.isPresent() && nextEdge.isPresent()) {
                graph.addEdge(graph.getOrder(currentEdge.get()).get().getOrderId(), currentEdge.get().getFirstNode(), nextEdge.get().getFirstNode());
            }
        }

        System.out.println("M2 after 1st iter");
        System.out.println(graph.getProductionTimeFor2("M2", 0) + "R-Prod" + graph.getRealProductionTimeFor("M2", 0)
                + " " + graph.getReleaseTimeFor2("M2", 0)
                + " R" + graph.getRealReleaseTimeFor2("M2", 0)
                + " " + graph.getDueDateFor2("M2", 0)
                + " " + graph.getMinimizedLatenessFor("M2", 0));
        System.out.println(graph.getProductionTimeFor2("M2", 1) + "R-Prod" + graph.getRealProductionTimeFor("M2", 1)
                + " " + graph.getReleaseTimeFor2("M2", 1)
                + " R" + graph.getRealReleaseTimeFor2("M2", 1)
                + " " + graph.getDueDateFor2("M2", 1)
                + " " + graph.getMinimizedLatenessFor("M2", 1));
        System.out.println(graph.getProductionTimeFor2("M2", 2) + "R-Prod" + graph.getRealProductionTimeFor("M2", 2)
                + " " + graph.getReleaseTimeFor2("M2", 2)
                + " R" + graph.getRealReleaseTimeFor2("M2", 2)
                + " " + graph.getDueDateFor2("M2", 2)
                + " " + graph.getMinimizedLatenessFor("M2", 2));
        System.out.println("MinimizedLateness for M2 -> " + graph.getRealMinimizedMaxLatenessFor("M2"));
        System.out.println("M3 after 1st iter");
        System.out.println(graph.getProductionTimeFor2("M3", 0) + "R-Prod" + graph.getRealProductionTimeFor("M3", 0)
                + " " + graph.getReleaseTimeFor2("M3", 0)
                + " R" + graph.getRealReleaseTimeFor2("M3", 0)
                + " " + graph.getDueDateFor2("M3", 0)
                + " " + graph.getMinimizedLatenessFor("M3", 0));
        System.out.println(graph.getProductionTimeFor2("M3", 1) + "R-Prod" + graph.getRealProductionTimeFor("M3", 1)
                + " " + graph.getReleaseTimeFor2("M3", 1)
                + " R" + graph.getRealReleaseTimeFor2("M3", 1)
                + " " + graph.getDueDateFor2("M3", 1)
                + " " + graph.getMinimizedLatenessFor("M3", 1));
        System.out.println("MinimizedLateness for M3 -> " + graph.getRealMinimizedMaxLatenessFor("M3"));
        System.out.println("MinimizedLateness for M4 -> " + graph.getRealMinimizedMaxLatenessFor("M4"));


        ResultSubgraph resSubFor2 = graph.getRealMinimizedMaxLatenessFor("M2");
        List<OrderInGraph> orders2 = resSubFor2.getResultSubgraph();
        for(int indexOfOrder = 0; indexOfOrder < orders2.size() - 1; indexOfOrder++) {
            Optional<Edge> currentEdge = graph.getEdgeFor("M2", orders2.get(indexOfOrder).getOrderId());
            Optional<Edge> nextEdge = graph.getEdgeFor("M2", orders2.get(indexOfOrder + 1).getOrderId());
            if(currentEdge.isPresent() && nextEdge.isPresent()) {
                graph.addEdge(graph.getOrder(currentEdge.get()).get().getOrderId(), currentEdge.get().getFirstNode(), nextEdge.get().getFirstNode());
            }
        }

        System.out.println("makespan now:" + graph.getMakespan());
        System.out.println("M3");
        System.out.println(graph.getProductionTimeFor2("M3", 0) + " " + graph.getRealReleaseTimeFor2("M3", 0)
                + " " + graph.getDueDateFor2("M3", 0)
                + " " + graph.getRealMaxLatenessFor("M3", 0));
        System.out.println(graph.getRealProductionTimeFor("M3", 1) + " " + graph.getRealReleaseTimeFor2("M3", 1)
                + " " + graph.getDueDateFor2("M3", 1)
                + " " + graph.getRealMaxLatenessFor("M3", 1));
        System.out.println("MinimizedLateness for M3 -> " + graph.getRealMinimizedMaxLatenessFor("M3"));
        System.out.println("R-lateness: M3 0 -> " + graph.getRealMaxLatenessFor("M3", 0));
        System.out.println("R-lateness: M3 1 -> " + graph.getRealMaxLatenessFor("M3", 1));
        System.out.println("MinimizedLateness for M4 -> " + graph.getRealMinimizedMaxLatenessFor("M4"));

        */
        return null;


    }


}
