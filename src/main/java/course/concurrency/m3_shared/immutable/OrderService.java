package course.concurrency.m3_shared.immutable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class OrderService {

    private final Map<Long, Order> currentOrders = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(0L);

    private long nextId() {
        return nextId.incrementAndGet();
    }

    public long createOrder(List<Item> items) {
        long id = nextId();
        currentOrders.put(id, new Order(id, items));
        return id;
    }

    public void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
        Order order = currentOrders.computeIfPresent(orderId, (key, value) -> value.withPaymentInfo(paymentInfo));
        if (order.checkStatus()) {
            deliver(order);
        }
    }

    public void setPacked(long orderId) {
        Order order = currentOrders.computeIfPresent(orderId, (key, value) -> value.withPacked(true));
        if (order.checkStatus()) {
            deliver(order);
        }
    }

    private void deliver(Order order) {
        if (!isDelivered(order.getId())) {
            currentOrders.computeIfPresent(order.getId(), (key, value) -> value.withStatus(Order.Status.DELIVERED));
        }
    }

    public Order get(long id) {
        return currentOrders.get(id);
    }

    public boolean isDelivered(long orderId) {
        return currentOrders.get(orderId).getStatus().equals(Order.Status.DELIVERED);
    }
}
