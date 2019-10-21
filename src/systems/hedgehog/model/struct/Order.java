package systems.hedgehog.model.struct;

import java.util.List;

public class Order {

    private List<String> allOrders;

    public List<String> getAllOrders() {
        return allOrders;
    }

    public void setAllOrders(List<String> allOrders) {
        this.allOrders = allOrders;
    }

    public Order(List<String> allOrders) {
        this.allOrders = allOrders;
    }
}
