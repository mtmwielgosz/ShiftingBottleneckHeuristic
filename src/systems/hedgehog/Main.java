package systems.hedgehog;

import systems.hedgehog.algorithm.Algorithm;
import systems.hedgehog.algorithm.ShiftingBottleneckHeuristic;
import systems.hedgehog.factory.GraphFactory;
import systems.hedgehog.model.graph.Graph;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Algorithm shiftingBottleneck = new ShiftingBottleneckHeuristic();
        Graph graph = GraphFactory.generateGraph(args[0]);
        System.out.println(graph);
        shiftingBottleneck.findScheduling(graph);
    }
}
