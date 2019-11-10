package systems.hedgehog.model.result;

public class SchedulingResult {

    private final String job;
    private final String machine;
    private final int startTime;
    private final int finishTime;

    public SchedulingResult(String job, String machine, int startTime, int finishTime) {
        this.job = job;
        this.machine = machine;
        this.startTime = startTime;
        this.finishTime = finishTime;
    }

    public String getJob() {
        return job;
    }

    public String getMachine() {
        return machine;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getFinishTime() {
        return finishTime;
    }

    @Override
    public String toString() {
        return job + " - " + machine + ", start: " + startTime + ", finish: " + finishTime;
    }

    public String toStringInFile() {
        return job + " " + startTime + " " + finishTime;
    }
}
