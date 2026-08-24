package model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int orderId;
    private User user;
    private Product product;
    private int quantity;
    private double totalPrice;
    private LocalDateTime orderDate;

    private List<OrderItem> orderItems;

    public Order(int orderId, User user, Product product, int quantity, double totalPrice, LocalDateTime orderDate) {
        this.orderId = orderId;
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
    }
}
