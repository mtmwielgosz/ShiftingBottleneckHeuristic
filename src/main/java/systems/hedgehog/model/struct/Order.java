package systems.hedgehog.model.struct;

import systems.hedgehog.model.graph.subelement.Edge;
import java.util.LinkedList;
import java.util.List;

public class Order {

    private final int orderId;
    private final List<Edge> jobsInOrder;

    public Order(int orderId) {
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
