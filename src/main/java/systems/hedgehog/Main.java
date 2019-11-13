package systems.hedgehog;

import systems.hedgehog.algorithm.Algorithm;
import systems.hedgehog.algorithm.impl.ShiftingBottleneckHeuristic;
import systems.hedgehog.algorithm.impl.SortedAgingEffect;
import systems.hedgehog.factory.GraphFactory;
import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.result.SchedulingResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static final String RESULT_FOR = "result_for_";

    public static void main(String[] args) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get("input"))) {
            List<Path> inputFiles = paths.filter(Files::isRegularFile).collect(Collectors.toList());
            for(Path inputFile : inputFiles) {
                printResult(new ShiftingBottleneckHeuristic(), inputFile);
            }
        }
    }

    private static void printResult(Algorithm algorithm, Path inputFile) throws IOException {
        Graph graph = GraphFactory.generateGraph(inputFile);
        System.out.println(graph);
        List<SchedulingResult> results = algorithm.findSchedulingWithConsoleLogs(graph);
        for(SchedulingResult result : results) {
            System.out.println(result);
        }
        Files.write(Paths.get("output/" + RESULT_FOR + inputFile.getFileName()).toAbsolutePath(), results.stream().map(SchedulingResult::toStringInFile).collect(Collectors.joining("\n")).getBytes());
        System.out.println(new GanttChart("Scheduling for " + inputFile.getFileName(), results));

    }

}
