package systems.hedgehog.model.result;

import java.util.Date;

public class SchedulingResult {

    private String job;
    private Date startProcessingDate;
    private Date endProcessingDate;
    private String machine;

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public Date getStartProcessingDate() {
        return startProcessingDate;
    }

    public void setStartProcessingDate(Date startProcessingDate) {
        this.startProcessingDate = startProcessingDate;
    }

    public Date getEndProcessingDate() {
        return endProcessingDate;
    }

    public void setEndProcessingDate(Date endProcessingDate) {
        this.endProcessingDate = endProcessingDate;
    }

    public String getMachine() {
        return machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public SchedulingResult(String job, Date startProcessingDate, Date endProcessingDate, String machine) {
        this.job = job;
        this.startProcessingDate = startProcessingDate;
        this.endProcessingDate = endProcessingDate;
        this.machine = machine;
    }

}
