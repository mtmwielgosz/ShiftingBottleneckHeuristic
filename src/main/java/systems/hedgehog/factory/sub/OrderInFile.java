package systems.hedgehog.factory.sub;

import java.util.List;

public class OrderInFile {

    private final int orderId;
    private final List<String> allJobs;

    public List<String> getAllJobs() {
        return allJobs;
    }

    public int getOrderId() {
        return orderId;
    }

    public OrderInFile(int orderId, List<String> allJobs) {
        this.orderId = orderId;
        this.allJobs = allJobs;
    }
}
