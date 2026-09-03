package dao;

import model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CategoryDAO {
    private static final Map<Integer, Category> fallbackCategories = new LinkedHashMap<>();
    private static int nextId = 1;
    private boolean useDb = true;

    public CategoryDAO() {
        this.useDb = isDatabaseAvailable();
    }

    public CategoryDAO(boolean useDb) {
        this.useDb = useDb;
    }

    private boolean isDatabaseAvailable() {
        try (Connection ignored = DatabaseConnection.getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public synchronized Category create(Category category) {
        if (category == null) throw new IllegalArgumentException("Category cannot be null.");
        if (useDb) {
            String sql = "INSERT INTO categories (name) VALUES (?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, category.getName());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setId(rs.getInt(1));
                        return category;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create category", e);
            }
        }

        category.setId(nextId++);
        fallbackCategories.put(category.getId(), category);
        return category;
    }

    public synchronized List<Category> findAll() {
        if (useDb) {
            String sql = "SELECT id, name FROM categories ORDER BY name ASC";
            List<Category> categories = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(rs.getInt("id"), rs.getString("name")));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load categories", e);
            }
            return categories;
        }
        return new ArrayList<>(fallbackCategories.values());
    }

    public synchronized Category findById(int id) {
        if (useDb) {
            String sql = "SELECT id, name FROM categories WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Category(rs.getInt("id"), rs.getString("name"));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find category", e);
            }
            return null;
        }
        return fallbackCategories.get(id);
    }

    public synchronized Category findByName(String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return null;
        if (useDb) {
            String sql = "SELECT id, name FROM categories WHERE name = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, trimmed);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Category(rs.getInt("id"), rs.getString("name"));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find category by name", e);
            }
            return null;
        }

        for (Category c : fallbackCategories.values()) {
            if (c.getName().equalsIgnoreCase(trimmed)) return c;
        }
        return null;
    }

    public synchronized void update(int id, Category category) {
        if (category == null) throw new IllegalArgumentException("Category cannot be null.");
        if (useDb) {
            String sql = "UPDATE categories SET name = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, category.getName());
                ps.setInt(2, id);
                if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Category not found: " + id);
                return;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update category", e);
            }
        }

        if (!fallbackCategories.containsKey(id)) throw new IllegalArgumentException("Category not found: " + id);
        category.setId(id);
        fallbackCategories.put(id, category);
    }

    public synchronized void deleteById(int id) {
        if (useDb) {
            String sql = "DELETE FROM categories WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Category not found: " + id);
                return;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete category", e);
            }
        }
        fallbackCategories.remove(id);
    }
}
