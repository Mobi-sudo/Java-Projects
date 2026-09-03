package dao;

import model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductDAO {
    private static final Map<Integer, Product> fallbackProducts = new LinkedHashMap<>();
    private static int nextId = 1;
    private boolean useDb = true;

    public ProductDAO() {
        this.useDb = isDatabaseAvailable();
    }

    public ProductDAO(boolean useDb) {
        this.useDb = useDb;
    }

    private boolean isDatabaseAvailable() {
        try (Connection ignored = DatabaseConnection.getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public synchronized int save(Product product) {
        if (product == null) throw new IllegalArgumentException("Product cannot be null.");
        if (useDb) {
            String sql = "INSERT INTO products (name, description, price, stock_quantity, category_id) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, product.getName());
                ps.setString(2, product.getDescription());
                ps.setDouble(3, product.getPrice());
                ps.setInt(4, product.getStockQuantity());
                if (product.getCategoryId() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, product.getCategoryId());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        product.setId(id);
                        return id;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save product", e);
            }
        }

        int id = nextId++;
        product.setId(id);
        fallbackProducts.put(id, product);
        return id;
    }

    public synchronized List<Product> findAll() {
        if (useDb) {
            String sql = "SELECT id, name, description, price, stock_quantity, category_id FROM products";
            List<Product> products = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load products", e);
            }
            return products;
        }
        return new ArrayList<>(fallbackProducts.values());
    }

    public synchronized Product findById(int id) {
        if (useDb) {
            String sql = "SELECT id, name, description, price, stock_quantity, category_id FROM products WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find product", e);
            }
            return null;
        }
        return fallbackProducts.get(id);
    }

    public synchronized List<Product> findByCategoryId(int categoryId) {
        if (useDb) {
            String sql = "SELECT id, name, description, price, stock_quantity, category_id FROM products WHERE category_id = ?";
            List<Product> products = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, categoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        products.add(mapRow(rs));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load category products", e);
            }
            return products;
        }
        List<Product> result = new ArrayList<>();
        for (Product p : fallbackProducts.values()) {
            if (p.getCategoryId() != null && p.getCategoryId() == categoryId) result.add(p);
        }
        return result;
    }

    public synchronized List<Product> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return findAll();
        String pattern = "%" + keyword.trim() + "%";
        if (useDb) {
            String sql = "SELECT id, name, description, price, stock_quantity, category_id FROM products WHERE name LIKE ? OR description LIKE ?";
            List<Product> result = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) result.add(mapRow(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to search products", e);
            }
            return result;
        }
        List<Product> result = new ArrayList<>();
        for (Product p : fallbackProducts.values()) {
            String haystack = (p.getName() + " " + p.getDescription()).toLowerCase();
            if (haystack.contains(keyword.trim().toLowerCase())) result.add(p);
        }
        return result;
    }

    public synchronized void update(int id, Product product) {
        if (product == null) throw new IllegalArgumentException("Product cannot be null.");
        if (useDb) {
            String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock_quantity = ?, category_id = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, product.getName());
                ps.setString(2, product.getDescription());
                ps.setDouble(3, product.getPrice());
                ps.setInt(4, product.getStockQuantity());
                if (product.getCategoryId() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, product.getCategoryId());
                ps.setInt(6, id);
                if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Product not found: " + id);
                return;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update product", e);
            }
        }
        if (!fallbackProducts.containsKey(id)) throw new IllegalArgumentException("Product not found: " + id);
        product.setId(id);
        fallbackProducts.put(id, product);
    }

    public synchronized void deleteById(int id) {
        if (useDb) {
            String sql = "DELETE FROM products WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Product not found: " + id);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete product", e);
            }
            return;
        }
        fallbackProducts.remove(id);
    }

    public synchronized boolean reduceStockIfAvailable(int productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        if (useDb) {
            String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quantity);
                ps.setInt(2, productId);
                ps.setInt(3, quantity);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update stock", e);
            }
        }
        Product p = fallbackProducts.get(productId);
        if (p == null || p.getStockQuantity() < quantity) return false;
        p.setStockQuantity(p.getStockQuantity() - quantity);
        return true;
    }

    public synchronized void addStock(int productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        if (useDb) {
            String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quantity);
                ps.setInt(2, productId);
                if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Product not found: " + productId);
                return;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to add stock", e);
            }
        }
        Product p = fallbackProducts.get(productId);
        if (p == null) throw new IllegalArgumentException("Product not found: " + productId);
        p.setStockQuantity(p.getStockQuantity() + quantity);
    }

    public synchronized boolean isAvailable(int productId, int quantity) {
        Product p = findById(productId);
        return p != null && p.getStockQuantity() >= quantity;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Integer categoryId = rs.getObject("category_id") == null ? null : rs.getInt("category_id");
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getInt("stock_quantity"),
                categoryId
        );
    }
}
