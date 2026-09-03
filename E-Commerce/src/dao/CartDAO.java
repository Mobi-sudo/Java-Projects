package dao;

import model.CartItem;
import model.User;
import model.Product;

import java.sql.*;
import java.util.*;

public class CartDAO {
    private final Map<Integer, List<CartItem>> carts = new HashMap<>();
    private boolean useDb = true;

    public CartDAO() {
        this.useDb = isDatabaseAvailable();
    }

    public CartDAO(boolean useDb) {
        this.useDb = useDb;
    }

    private boolean isDatabaseAvailable() {
        try (Connection ignored = DatabaseConnection.getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public synchronized List<CartItem> getCartForUser(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (useDb) {
            final String sql = "SELECT product_id, quantity FROM cart_items WHERE user_id = ?";
            List<CartItem> items = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, user.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int pid = rs.getInt("product_id");
                        int qty = rs.getInt("quantity");
                        Product p = new ProductDAO().findById(pid);
                        if (p != null) items.add(new CartItem(user, p, qty));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return items;
        }
        return carts.computeIfAbsent(user.getId(), k -> new ArrayList<>());
    }

    public synchronized void saveCartForUser(User user, List<CartItem> items) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (items == null) items = Collections.emptyList();
        if (useDb) {
            final String deleteSql = "DELETE FROM cart_items WHERE user_id = ?";
            final String insertSql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                        del.setInt(1, user.getId());
                        del.executeUpdate();
                    }
                    try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                        for (CartItem item : items) {
                            if (item == null || item.getProduct() == null || item.getQuantity() <= 0) {
                                throw new IllegalArgumentException("Cart contains an invalid item");
                            }
                            ins.setInt(1, user.getId());
                            ins.setInt(2, item.getProduct().getId());
                            ins.setInt(3, item.getQuantity());
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                    conn.commit();
                } catch (SQLException | RuntimeException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackError) {
                        e.addSuppressed(rollbackError);
                    }
                    throw e;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save cart", e);
            }
            return;
        }
        carts.put(user.getId(), new ArrayList<>(items));
    }

    public synchronized void clearCart(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (useDb) {
            final String sql = "DELETE FROM cart_items WHERE user_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, user.getId());
                ps.executeUpdate();
                return;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        carts.remove(user.getId());
    }
}
