package systems.hedgehog;

import systems.hedgehog.algorithm.Algorithm;
import systems.hedgehog.algorithm.ShiftingBottleneckHeuristic;
import systems.hedgehog.factory.GraphFactory;
import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.result.SchedulingResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static final String RESULT_FOR = "result_for_";

    public static void main(String[] args) throws IOException {
        Algorithm shiftingBottleneck = new ShiftingBottleneckHeuristic();
        Graph graph = GraphFactory.generateGraph(args[0]);
        System.out.println(graph);
        List<SchedulingResult> results = shiftingBottleneck.findScheduling(graph);
        for(SchedulingResult result : results) {
            System.out.println(result);
        }
        Files.write(Paths.get(RESULT_FOR + args[0]).toAbsolutePath(), results.stream().map(SchedulingResult::toStringInFile).collect(Collectors.joining("\n")).getBytes());

    }
}
