package systems.hedgehog.algorithm;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.graph.subelement.Edge;
import systems.hedgehog.model.result.SchedulingResult;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

        List<Edge> edges = graph.latenessEdges("M1", 0, 0, new LinkedList<>());

        return null;
    }


}
