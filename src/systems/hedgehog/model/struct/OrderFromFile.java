package systems.hedgehog.model.struct;

import java.util.List;

public class OrderFromFile {

    private int orderId;
    private List<String> allJobs;

    public List<String> getAllJobs() {
        return allJobs;
    }

    public int getOrderId() {
        return orderId;
    }

    public OrderFromFile(int orderId, List<String> allJobs) {
        this.orderId = orderId;
        this.allJobs = allJobs;
    }
}
