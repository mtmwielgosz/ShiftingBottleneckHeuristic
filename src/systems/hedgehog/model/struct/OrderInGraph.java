package systems.hedgehog.model.struct;

import systems.hedgehog.model.graph.subelement.Edge;
import java.util.LinkedList;
import java.util.List;

public class OrderInGraph {

    private int orderId;
    private List<Edge> jobsInOrder;

    public OrderInGraph(int orderId) {
        this.orderId = orderId;
        this.jobsInOrder = new LinkedList<>();
    }

    public int getOrderId() {
        return orderId;
    }

    public List<Edge> getJobsInOrder() {
        return jobsInOrder;
    }

    public void addJob(Edge job) {
        jobsInOrder.add(job);
    }
}
