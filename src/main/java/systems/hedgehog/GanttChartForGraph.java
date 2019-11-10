package systems.hedgehog;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import systems.hedgehog.model.result.SchedulingResult;

import javax.swing.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GanttChartForGraph extends JFrame {

        private static final long serialVersionUID = 1L;

        private GanttChartForGraph(String title, List<SchedulingResult> results) {
            super(title);
            IntervalCategoryDataset dataset = getDataset(results);
            JFreeChart chart = ChartFactory.createGanttChart(title,"Machines","Timeline",
                    dataset, false, false, false);
            ChartPanel panel = new ChartPanel(chart);
            setContentPane(panel);
        }

        private IntervalCategoryDataset getDataset(List<SchedulingResult> results) {

            int maxMakespan = results.stream().flatMapToInt(result -> IntStream.of(result.getFinishTime())).max().orElse(0);

            TaskSeries scheduling = new TaskSeries("Scheduling");
            scheduling.add(new Task("Whole makespan",
                    Date.from(LocalDate.of(2020,1,1).atStartOfDay().toInstant(ZoneOffset.UTC)),
                    Date.from(LocalDate.of(2020, 1,1).plusDays(maxMakespan).atStartOfDay().toInstant(ZoneOffset.UTC))
            ));

            Set<String> machines = results.stream().map(SchedulingResult::getMachine).collect(Collectors.toSet());
            for(String machine: machines) {
                List<SchedulingResult> resultsForMachine = results.stream().filter(result -> result.getMachine().equals(machine)).collect(Collectors.toList());
                Task dummyTask = new Task(machine, Date.from(LocalDate.of(2020, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)),
                        Date.from(LocalDate.of(2020, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)));
                for(SchedulingResult result : resultsForMachine) {
                    Task subTask = new Task(result.getMachine(), Date.from(LocalDate.of(2020, 1, 1).plusDays(result.getStartTime()).atStartOfDay().toInstant(ZoneOffset.UTC)),
                            Date.from(LocalDate.of(2020, 1, 1).plusDays(result.getFinishTime()).atStartOfDay().toInstant(ZoneOffset.UTC)));
                    dummyTask.addSubtask(subTask);
                }
                scheduling.add(dummyTask);
            }
            TaskSeriesCollection dataset = new TaskSeriesCollection();
            dataset.add(scheduling);
            return dataset;
        }

        static void run(String title, List<SchedulingResult> results) {
            SwingUtilities.invokeLater(() -> {
                GanttChartForGraph example = new GanttChartForGraph(title, results);
                example.setSize(1000, 500);
                example.setLocationRelativeTo(null);
                example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                example.setVisible(true);
            });
        }
    }


