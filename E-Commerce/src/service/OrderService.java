package service;

import dao.OrderDAO;
import dao.ProductDAO;
import dao.CartDAO;
import model.Order;
import model.OrderItem;
import model.Product;
import model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private final AuthenticationService auth;
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final CartDAO cartDAO;

    public OrderService(AuthenticationService auth, OrderDAO orderDAO, ProductDAO productDAO, CartDAO cartDAO) {
        this.auth = auth;
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.cartDAO = cartDAO;
    }

    private User requireAuthenticatedCustomer() {
        if (!auth.isAuthenticated()) throw new SecurityException("Authentication required");
        User u = auth.getCurrentUser();
        if (!u.isActive()) throw new SecurityException("Account not active");
        return u;
    }

    public Order checkout() {
        User u = requireAuthenticatedCustomer();
        List<OrderItem> items = new ArrayList<>();
        for (var ci : cartDAO.getCartForUser(u)) {
            Product p = ci.getProduct();
            if (p == null) throw new IllegalStateException("Cart contains invalid product");
            // snapshot unit price
            items.add(new OrderItem(p, ci.getQuantity(), p.getPrice()));
        }
        Order order = new Order(0, u, items, LocalDateTime.now());
        int id = orderDAO.save(order);
        // clear cart
        cartDAO.clearCart(u);
        return order;
    }

    public List<Order> getMyOrders() {
        User u = requireAuthenticatedCustomer();
        return orderDAO.findByUserId(u.getId());
    }

    // Admin-only
    public List<Order> getAllOrders() {
        if (!auth.isAuthenticated() || !auth.isAdmin()) throw new SecurityException("Admin required");
        return orderDAO.findAll();
    }

    public void updateOrderStatus(int orderId, String statusStr) {
        if (!auth.isAuthenticated() || !auth.isAdmin()) throw new SecurityException("Admin required");
        Order o = orderDAO.findById(orderId);
        if (o == null) throw new IllegalArgumentException("Order not found");
        model.OrderStatus newStatus;
        try {
            newStatus = model.OrderStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status: " + statusStr);
        }
        model.OrderStatus current = o.getStatus();
        if (!isValidTransition(current, newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + current + " to " + newStatus);
        }
        orderDAO.updateStatus(orderId, newStatus);
        o.setStatus(newStatus);
    }

    private boolean isValidTransition(model.OrderStatus from, model.OrderStatus to) {
        if (from == to) return true;
        switch (from) {
            case PENDING:
                return to == model.OrderStatus.PROCESSING || to == model.OrderStatus.CANCELLED;
            case PROCESSING:
                return to == model.OrderStatus.SHIPPED || to == model.OrderStatus.CANCELLED;
            case SHIPPED:
                return to == model.OrderStatus.DELIVERED;
            case DELIVERED:
                return false; // terminal
            case CANCELLED:
                return false; // terminal
            default:
                return false;
        }
    }
}
