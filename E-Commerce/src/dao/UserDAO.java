package dao;

import model.User;
import model.UserRole;
import model.AccountStatus;


import java.sql.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserDAO {
    private static final Map<Integer, User> fallbackUsers = new LinkedHashMap<>();
    private static int nextId = 1;
    private boolean useDb = true;

    public UserDAO() {
        this.useDb = isDatabaseAvailable();
    }

    public UserDAO(boolean useDb) {
        this.useDb = useDb;
    }

    private boolean isDatabaseAvailable() {
        try (Connection ignored = DatabaseConnection.getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public User createUser(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null.");

        if (useDb) {
            String sql = "INSERT INTO users (name, email, password_hash, role, account_status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPasswordHash());
                ps.setString(4, user.getRole().name());
                ps.setString(5, user.getStatus().name());
                ps.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt() == null ? LocalDateTime.now() : user.getCreatedAt()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
                return user;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create user", e);
            }
        }

        user.setId(nextId++);
        fallbackUsers.put(user.getId(), user);
        return user;
    }

    public User findByEmail(String email) {
        if (email == null) return null;

        if (useDb) {
            String sql = "SELECT id, name, email, password_hash, role, account_status, created_at FROM users WHERE email = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find user by email", e);
            }
            return null;
        }

        for (User u : fallbackUsers.values()) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) return u;
        }
        return null;
    }

    public User findByUsername(String username) {
        if (username == null) return null;
        if (useDb) {
            String sql = "SELECT id, name, email, password_hash, role, account_status, created_at FROM users WHERE name = ? OR email = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                String value = username.trim();
                ps.setString(1, value);
                ps.setString(2, value);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find user by username", e);
            }
            return null;
        }
        for (User u : fallbackUsers.values()) {
            if (u.getName().equalsIgnoreCase(username.trim()) || u.getEmail().equalsIgnoreCase(username.trim())) return u;
        }
        return null;
    }

    public User findById(int id) {
        if (useDb) {
            String sql = "SELECT id, name, email, password_hash, role, account_status, created_at FROM users WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find user by id", e);
            }
            return null;
        }
        return fallbackUsers.get(id);
    }

    private User mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        UserRole role = UserRole.valueOf(rs.getString("role"));
        AccountStatus status = AccountStatus.valueOf(rs.getString("account_status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new User(id, name, email, passwordHash, role, status,
                createdAt == null ? null : createdAt.toLocalDateTime());
    }
}
