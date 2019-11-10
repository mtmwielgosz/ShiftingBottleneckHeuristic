package systems.hedgehog.algorithm;

import systems.hedgehog.model.graph.Graph;
import systems.hedgehog.model.result.SchedulingResult;

import java.util.List;

public interface Algorithm {

    List<SchedulingResult> findScheduling(Graph graph);
    List<SchedulingResult> findSchedulingWithConsoleLogs(Graph graph);
}
