package systems.hedgehog.algorithm;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.result.SchedulingResult;

import java.util.LinkedList;
import java.util.List;

public class ShiftingBottleneckHeuristic implements Algorithm {

    @Override
    public List<SchedulingResult> findScheduling(Graph graph) {

        System.out.println(graph.getMakespan());
        System.out.println(graph.getProductionTimeFor("M1", 0) + " " + graph.getReleaseTimeFor("M1", 0)
                + " " + graph.getDueDateFor("M1", 0));
        System.out.println(graph.getProductionTimeFor("M1", 1) + " " + graph.getReleaseTimeFor("M1", 1)
                + " " + graph.getDueDateFor("M1", 1));
        System.out.println(graph.getProductionTimeFor("M1", 2) + " " + graph.getReleaseTimeFor("M1", 2)
                + " " + graph.getDueDateFor("M1", 2));

        System.out.println(graph.getProductionTimeFor("M2", 0) + " " + graph.getReleaseTimeFor("M2", 0)
                + " " + graph.getDueDateFor("M2", 0));
        System.out.println(graph.getProductionTimeFor("M2", 1) + " " + graph.getReleaseTimeFor("M2", 1)
                + " " + graph.getDueDateFor("M2", 1));
        System.out.println(graph.getProductionTimeFor("M2", 2) + " " + graph.getReleaseTimeFor("M2", 2)
                + " " + graph.getDueDateFor("M2", 2));

        List<Edge> edges0 = graph.getMaxLatenessEdgesFor("M1", 0, 0, new LinkedList<>());

        List<Edge> edges1 = graph.getMaxLatenessEdgesFor("M1", 1, 0, new LinkedList<>());

        List<Edge> edges2 = graph.getMaxLatenessEdgesFor("M1", 2, 0, new LinkedList<>());

        List<Edge> edges00 = graph.getMaxLatenessEdgesForEndingOrder("M1", 0, 0, new LinkedList<>());

        List<Edge> edges01 = graph.getMaxLatenessEdgesForEndingOrder("M1", 1, 0, new LinkedList<>());

        List<Edge> edges02 = graph.getMaxLatenessEdgesForEndingOrder("M1", 2, 0, new LinkedList<>());

        return null;
    }


}
