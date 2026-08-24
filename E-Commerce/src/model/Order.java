package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private int orderId; // 0 if not persisted
    private User user;
    private final List<OrderItem> orderItems;
    private double totalPrice;
    private LocalDateTime orderDate;
    private OrderStatus status;

    public Order(int orderId, User user, LocalDateTime orderDate) {
        this(orderId, user, new ArrayList<>(), orderDate, OrderStatus.PENDING);
    }

    public Order(int orderId, User user, List<OrderItem> orderItems, LocalDateTime orderDate) {
        this(orderId, user, orderItems, orderDate, OrderStatus.PENDING);
    }

    public Order(int orderId, User user, List<OrderItem> orderItems, LocalDateTime orderDate, OrderStatus status) {
        setOrderId(orderId);
        setUser(user);
        this.orderItems = new ArrayList<>();
        setOrderDate(orderDate);
        setStatus(status);
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                addItem(item);
            }
        }
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        if (orderId < 0) {
            throw new IllegalArgumentException("Order ID cannot be negative.");
        }
        this.orderId = orderId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        this.user = user;
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems.clear();
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                addItem(item);
            }
        }
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Order item cannot be null.");
        }
        orderItems.add(item);
        totalPrice = calculateTotalPrice();
    }

    public void removeItem(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        orderItems.removeIf(item -> item.getProduct().equals(product));
        totalPrice = calculateTotalPrice();
    }

    public double getTotalPrice() {
        return totalPrice > 0 ? totalPrice : calculateTotalPrice();
    }

    public void setTotalPrice(double totalPrice) {
        if (totalPrice < 0) {
            throw new IllegalArgumentException("Total price cannot be negative.");
        }
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        if (orderDate == null) {
            throw new IllegalArgumentException("Order date cannot be null.");
        }
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        if (status == null) throw new IllegalArgumentException("Order status cannot be null");
        this.status = status;
    }

    public Product getProduct() {
        return orderItems.isEmpty() ? null : orderItems.get(0).getProduct();
    }

    public int getQuantity() {
        int total = 0;
        for (OrderItem item : orderItems) {
            total += item.getQuantity();
        }
        return total;
    }

    private double calculateTotalPrice() {
        double sum = 0;
        for (OrderItem item : orderItems) {
            if (item.getProduct() != null) {
                sum += item.getUnitPrice() * item.getQuantity();
            }
        }
        return sum;
    }
}
