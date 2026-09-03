package dao;

import model.Order;
import model.OrderItem;
import model.Product;
import model.User;
import model.OrderStatus;

import java.sql.*;
import java.util.*;

public class OrderDAO {
    private final Map<Integer, Order> orders = new LinkedHashMap<>();
    private int nextId = 1;
    private boolean useDb = true;

    public OrderDAO() {
        this.useDb = isDatabaseAvailable();
    }

    public OrderDAO(boolean useDb) {
        this.useDb = useDb;
    }

    private boolean isDatabaseAvailable() {
        try (Connection ignored = DatabaseConnection.getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public synchronized int save(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        if (order.getUser().getId() <= 0) throw new IllegalArgumentException("Order user must be persisted");
        if (order.getOrderItems().isEmpty()) throw new IllegalArgumentException("Order must contain at least one item");
        if (useDb) {
            final String updateStockSql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
            final String insertOrder = "INSERT INTO orders (user_id, total_price, status, created_at) VALUES (?, ?, ?, ?)";
            final String insertItem = "INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // reduce stock first (atomic checks) using same transaction
                    try (PreparedStatement psStock = conn.prepareStatement(updateStockSql)) {
                        for (OrderItem oi : order.getOrderItems()) {
                            psStock.setInt(1, oi.getQuantity());
                            psStock.setInt(2, oi.getProduct().getId());
                            psStock.setInt(3, oi.getQuantity());
                            int updated = psStock.executeUpdate();
                            if (updated == 0) {
                                conn.rollback();
                                throw new IllegalStateException("Insufficient stock for product id=" + oi.getProduct().getId());
                            }
                        }
                    }

                    // insert order
                    try (PreparedStatement psOrder = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                        psOrder.setInt(1, order.getUser().getId());
                        psOrder.setDouble(2, order.getTotalPrice());
                        psOrder.setString(3, order.getStatus().name());
                        psOrder.setTimestamp(4, Timestamp.valueOf(order.getOrderDate()));
                        psOrder.executeUpdate();
                        try (ResultSet rs = psOrder.getGeneratedKeys()) {
                            if (rs.next()) {
                                int oid = rs.getInt(1);
                                try (PreparedStatement psItem = conn.prepareStatement(insertItem)) {
                                    for (OrderItem oi : order.getOrderItems()) {
                                        psItem.setInt(1, oid);
                                        psItem.setInt(2, oi.getProduct().getId());
                                        psItem.setInt(3, oi.getQuantity());
                                        psItem.setDouble(4, oi.getUnitPrice());
                                        psItem.addBatch();
                                    }
                                    psItem.executeBatch();
                                }
                                conn.commit();
                                order.setOrderId(oid);
                                return oid;
                            } else {
                                conn.rollback();
                                throw new SQLException("Failed to obtain order id");
                            }
                        }
                    }

                } catch (SQLException | RuntimeException e) {
                    try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
                    throw e;
                } finally {
                    try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        // in-memory fallback: reduce stock via ProductDAO
        ProductDAO productDAO = new ProductDAO(false);
        for (OrderItem item : order.getOrderItems()) {
            if (!productDAO.isAvailable(item.getProduct().getId(), item.getQuantity())) {
                throw new IllegalStateException("Insufficient stock for product id=" + item.getProduct().getId());
            }
        }
        for (OrderItem item : order.getOrderItems()) {
            productDAO.reduceStockIfAvailable(item.getProduct().getId(), item.getQuantity());
        }
        int id = nextId++;
        orders.put(id, order);
        order.setOrderId(id);
        return id;
    }

    public synchronized List<Order> findAll() {
        if (useDb) {
            final String sql = "SELECT id, user_id, total_price, status, created_at FROM orders";
            List<Order> res = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int uid = rs.getInt("user_id");
                    double total = rs.getDouble("total_price");
                    OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    // load items
                    List<OrderItem> items = loadItems(conn, id);
                    // load user minimal
                    User u = new dao.UserDAO().findById(uid);
                    Order o = new Order(id, u, items, ts.toLocalDateTime(), status);
                    o.setTotalPrice(total);
                    res.add(o);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return res;
        }
        return new ArrayList<>(orders.values());
    }

    private List<OrderItem> loadItems(Connection conn, int orderId) throws SQLException {
        final String sql = "SELECT id, product_id, quantity, price_at_purchase FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int pid = rs.getInt("product_id");
                    int qty = rs.getInt("quantity");
                    double up = rs.getDouble("price_at_purchase");
                    Product p = new dao.ProductDAO().findById(pid);
                    items.add(new OrderItem(id, orderId, p, qty, up));
                }
            }
        }
        return items;
    }

    public synchronized Order findById(int id) {
        if (useDb) {
            final String sql = "SELECT id, user_id, total_price, status, created_at FROM orders WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int uid = rs.getInt("user_id");
                        double total = rs.getDouble("total_price");
                        OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
                        Timestamp ts = rs.getTimestamp("created_at");
                        List<OrderItem> items = loadItems(conn, id);
                        User u = new dao.UserDAO().findById(uid);
                        Order o = new Order(id, u, items, ts.toLocalDateTime(), status);
                        o.setTotalPrice(total);
                        return o;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
        return orders.get(id);
    }

    public synchronized List<Order> findByUserId(int userId) {
        if (useDb) {
            final String sql = "SELECT id FROM orders WHERE user_id = ?";
            List<Order> res = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        Order o = findById(id);
                        if (o != null) res.add(o);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return res;
        }
        List<Order> res = new ArrayList<>();
        for (Order o : orders.values()) if (o.getUser() != null && o.getUser().getId() == userId) res.add(o);
        return res;
    }

    public synchronized void updateStatus(int id, OrderStatus status) {
        if (useDb) {
            final String sql = "UPDATE orders SET status = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status.name());
                ps.setInt(2, id);
                if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Order not found: " + id);
                return;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        Order o = orders.get(id);
        if (o == null) throw new IllegalArgumentException("No such order");
        o.setStatus(status);
    }

    // NOTE: the previous update(int, Order) method has been removed. It only ever
    // touched the in-memory `orders` map and threw "No such order id" unconditionally
    // in DB mode, and nothing in the codebase called it. If a general-purpose order
    // update is needed later, add it back deliberately with real DB-mode support
    // rather than resurrecting the old in-memory-only version.
}
