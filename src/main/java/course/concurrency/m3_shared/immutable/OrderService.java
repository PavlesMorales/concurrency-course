package course.concurrency.m3_shared.immutable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class OrderService {

    private final Map<Long, Order> currentOrders = new HashMap<>();
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
        currentOrders.merge(
                orderId,
                currentOrders.get(orderId),
                (oldest, newest) ->
                        oldest.getPaymentInfo() != null ? oldest : newest.withPaymentInfo(paymentInfo));

        if (currentOrders.get(orderId).checkStatus()) {
            deliver(currentOrders.get(orderId));
        }
    }

    public void setPacked(long orderId) {
        currentOrders.merge(
                orderId,
                currentOrders.get(orderId),
                (oldest, newest) ->
                        oldest.isPacked() ? oldest : newest.withPacked(true));

        if (currentOrders.get(orderId).checkStatus()) {
            deliver(currentOrders.get(orderId));
        }
    }

    private void deliver(Order order) {
        currentOrders.merge(
                order.getId(),
                order,
                (oldest, newest) ->
                        isDelivered(oldest.getId()) ? oldest : newest.withStatus(Order.Status.DELIVERED));
    }

    public Order get(long id) {
        return currentOrders.get(id);
    }

    public boolean isDelivered(long orderId) {
        return currentOrders.get(orderId).getStatus().equals(Order.Status.DELIVERED);
    }
}
