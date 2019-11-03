package systems.hedgehog.algorithm;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.SecGraph;
import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.graph.subelement.Subgraph;
import systems.hedgehog.model.result.SchedulingResult;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ShiftingBottleneckHeuristic implements Algorithm {

    @Override
    public List<SchedulingResult> findScheduling(Graph graph) {

        System.out.println(graph.getMakespan());
        System.out.println(graph.getProductionTimeFor("M1", 0) + " " + graph.getReleaseTimeFor("M1", 0)
                + " " + graph.getDueDateFor("M1", 0)
                + " " + graph.getMinimizedLatenessFor("M1", 0));
        System.out.println(graph.getProductionTimeFor("M1", 1) + " " + graph.getReleaseTimeFor("M1", 1)
                + " " + graph.getDueDateFor("M1", 1)
                + " " + graph.getMinimizedLatenessFor("M1", 1));
        System.out.println(graph.getProductionTimeFor("M1", 2) + " " + graph.getReleaseTimeFor("M1", 2)
                + " " + graph.getDueDateFor("M1", 2)
                + " " + graph.getMinimizedLatenessFor("M1", 2));

        System.out.println(graph.getProductionTimeFor("M2", 0) + " " + graph.getReleaseTimeFor("M2", 0)
                + " " + graph.getDueDateFor("M2", 0)
                + " " + graph.getMinimizedLatenessFor("M2", 0));
        System.out.println(graph.getProductionTimeFor("M2", 1) + " " + graph.getReleaseTimeFor("M2", 1)
                + " " + graph.getDueDateFor("M2", 1)
                + " " + graph.getMinimizedLatenessFor("M2", 1));
        System.out.println(graph.getProductionTimeFor("M2", 2) + " " + graph.getReleaseTimeFor("M2", 2)
                + " " + graph.getDueDateFor("M2", 2)
                + " " + graph.getMinimizedLatenessFor("M2", 2));

        System.out.println(graph.getProductionTimeFor("M3", 0) + " " + graph.getReleaseTimeFor("M3", 0)
                + " " + graph.getDueDateFor("M3", 0)
                + " " + graph.getMinimizedLatenessFor("M3", 0));
        System.out.println(graph.getProductionTimeFor("M3", 1) + " " + graph.getReleaseTimeFor("M3", 1)
                + " " + graph.getDueDateFor("M3", 1)
                + " " + graph.getMinimizedLatenessFor("M3", 1));
        System.out.println(graph.getProductionTimeFor("M3", 2) + " " + graph.getReleaseTimeFor("M3", 2)
                + " " + graph.getDueDateFor("M3", 2)
                + " " + graph.getMinimizedLatenessFor("M3", 2));

        System.out.println(graph.getProductionTimeFor("M4", 0) + " " + graph.getReleaseTimeFor("M4", 0)
                + " " + graph.getDueDateFor("M4", 0)
                + " " + graph.getMinimizedLatenessFor("M4", 0));
        System.out.println(graph.getProductionTimeFor("M4", 1) + " " + graph.getReleaseTimeFor("M4", 1)
                + " " + graph.getDueDateFor("M4", 1)
                + " " + graph.getMinimizedLatenessFor("M4", 1));
        System.out.println(graph.getProductionTimeFor("M4", 2) + " " + graph.getReleaseTimeFor("M4", 2)
                + " " + graph.getDueDateFor("M4", 2)
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
            graph.addEdge(graph.getOrder(nextEdge).get().getOrderId(), currentEdge.getFirstNode(), nextEdge.getFirstNode());
        }


        SecGraph secGraph = new SecGraph(graph);
        System.out.println(secGraph.getMakespan());

        return null;
    }


}
