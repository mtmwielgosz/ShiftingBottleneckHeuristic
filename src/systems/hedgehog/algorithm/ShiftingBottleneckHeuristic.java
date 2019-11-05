package systems.hedgehog.algorithm;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.graph.subelement.Subgraph;
import systems.hedgehog.model.result.SchedulingResult;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ShiftingBottleneckHeuristic implements Algorithm {

    @Override
    public List<SchedulingResult> findScheduling(Graph graph) {

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

        System.out.println("M3");
        System.out.println(graph.getProductionTimeFor2("M3", 0) + " " + graph.getReleaseTimeFor2("M3", 0)
                + " " + graph.getDueDateFor2("M3", 0)
                + " " + graph.getMinimizedLatenessFor("M3", 0));
        System.out.println(graph.getProductionTimeFor2("M3", 1) + " " + graph.getReleaseTimeFor2("M3", 1)
                + " " + graph.getDueDateFor2("M3", 1)
                + " " + graph.getMinimizedLatenessFor("M3", 1));

        System.out.println("M4");
        System.out.println(graph.getProductionTimeFor2("M4", 1) + " " + graph.getReleaseTimeFor2("M4", 1)
                + " " + graph.getDueDateFor2("M4", 1)
                + " " + graph.getMinimizedLatenessFor("M4", 1));
        System.out.println(graph.getProductionTimeFor2("M4", 2) + " " + graph.getReleaseTimeFor2("M4", 2)
                + " " + graph.getDueDateFor2("M4", 2)
                + " " + graph.getMinimizedLatenessFor("M4", 2));

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

        List<Subgraph> allSubgraph = new LinkedList<>();
        allSubgraph.addAll(minimalizedSubgraphs1);
        allSubgraph.addAll(minimalizedSubgraphs2);
        allSubgraph.addAll(minimalizedSubgraphs3);
        allSubgraph.addAll(minimalizedSubgraphs4);

        Subgraph maxMakespan = allSubgraph.stream().max(Comparator.comparing(Subgraph::getMaxMakespan)).get();

        List<Edge> edges = maxMakespan.getEdges();

        for(int index = 0; index < edges.size() - 1; index++) {
            Edge currentEdge = edges.get(index);
            Edge nextEdge = edges.get(index + 1);
            graph.addEdge(graph.getOrder(currentEdge).get().getOrderId(), currentEdge.getFirstNode(), nextEdge.getFirstNode());
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

        System.out.println(" Order 0 Makespan:" + graph.getMakespanForOrder(0));
        System.out.println(" Order 1 Makespan:" + graph.getMakespanForOrder(1));
        System.out.println(" Order 2 Makespan:" + graph.getMakespanForOrder(2));



        return null;
    }


}
