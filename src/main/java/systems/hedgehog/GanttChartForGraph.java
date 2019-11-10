package systems.hedgehog;

import systems.hedgehog.model.result.SchedulingResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class GanttChartForGraph {

    private static final int BEFORE_LINE = 3;
    private static final String SEPARATOR = "|";
    private String title;
    private List<SchedulingResult> results;


    GanttChartForGraph(String title, List<SchedulingResult> results) {
        this.title = title;
        this.results = results;
    }

    @Override
    public String toString() {

        StringBuilder ganttChart = new StringBuilder(title + "\n");
        Set<String> machines = results.stream().map(SchedulingResult::getMachine).collect(Collectors.toSet());
        for(String machine: machines) {
            StringBuilder lineForMachine = new StringBuilder(machine + SEPARATOR);
            List<SchedulingResult> resultsForMachine = results.stream().filter(result -> result.getMachine().equals(machine)).collect(Collectors.toList());
            for(SchedulingResult result : resultsForMachine) {
                int lengthOfEmptyLineBefore = result.getStartTime() - (lineForMachine.length() - BEFORE_LINE);
                int lengthOfJobLine = result.getFinishTime() - result.getStartTime();
                lineForMachine.append(getLine(lengthOfEmptyLineBefore));
                lineForMachine.append(getLine(lengthOfJobLine, result.getJob()));
            }
            ganttChart.append(lineForMachine).append("|\n");
        }
        return ganttChart.toString();
    }

    private String getLine(int lineSize, String character) {
        if(lineSize <= 0) {
            return "";
        }
        StringBuilder line = new StringBuilder();
        for(int i = 0; i < lineSize; i++) {
            line.append(character);
        }
        return line.toString();
    }

    private String getLine(int lineSize) {
        return getLine(lineSize, "_");
    }

}


