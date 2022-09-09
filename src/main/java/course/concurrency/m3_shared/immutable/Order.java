package course.concurrency.m3_shared.immutable;

import java.util.*;

import static course.concurrency.m3_shared.immutable.Order.Status.IN_PROGRESS;
import static course.concurrency.m3_shared.immutable.Order.Status.NEW;

public final class Order {

    public enum Status {NEW, IN_PROGRESS, DELIVERED}

    private final Long id;
    private final List<Item> items;
    private final PaymentInfo paymentInfo;
    private final boolean isPacked;
    private final Status status;

    public Order(long id, List<Item> items) {
        this(id, items, null, false, NEW);
    }

    private Order(Long id, List<Item> items, PaymentInfo paymentInfo, boolean isPacked, Status status) {
        this.id = id;
        this.items = List.copyOf(items);
        this.paymentInfo = paymentInfo;
        this.isPacked = isPacked;
        this.status = status;
    }

    public boolean checkStatus() {
        return items != null && !items.isEmpty() && paymentInfo != null && isPacked;
    }

    public Long getId() {
        return id;
    }

    public List<Item> getItems() {
        return List.copyOf(items);
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public Order withPaymentInfo(PaymentInfo paymentInfo) {
        return new Order(this.id, this.items, paymentInfo, this.isPacked, IN_PROGRESS);
    }

    public boolean isPacked() {
        return isPacked;
    }

    public Order withPacked(boolean packed) {
        return new Order(this.id, this.items, this.paymentInfo, packed, IN_PROGRESS);

    }

    public Status getStatus() {
        return status;
    }

    public Order withStatus(Status status) {
        return new Order(this.id, this.items, this.paymentInfo, this.isPacked, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return isPacked == order.isPacked && Objects.equals(id, order.id) && Objects.equals(items, order.items) && Objects.equals(paymentInfo, order.paymentInfo) && status == order.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, items, paymentInfo, isPacked, status);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", items=" + items +
                ", paymentInfo=" + paymentInfo +
                ", isPacked=" + isPacked +
                ", status=" + status +
                '}';
    }
}
