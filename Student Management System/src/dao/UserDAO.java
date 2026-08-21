package dao;

import java.sql.*;
import model.User;

public class UserDAO {
    public boolean createUser(User u) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        String sql = "INSERT INTO users (username, password_hash, salt, role, student_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, u.getUsername());
            ps.setBytes(2, u.getPasswordHash());
            ps.setBytes(3, u.getSalt());
            ps.setString(4, u.getRole());
            if (u.getStudentId() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, u.getStudentId());
            int rows = ps.executeUpdate();
            if (rows == 1) {
                try (ResultSet rs = ps.getGeneratedKeys()){
                    if (rs.next()){
                        // generated id available but not necessary to return
                    }
                }
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            // duplicate username or FK violation
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public User findByUsername(String username) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;
        String sql = "SELECT user_id, username, password_hash, salt, role, student_id FROM users WHERE username = ?";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    int id = rs.getInt("user_id");
                    String user = rs.getString("username");
                    byte[] hash = rs.getBytes("password_hash");
                    byte[] salt = rs.getBytes("salt");
                    String role = rs.getString("role");
                    int sid = rs.getInt("student_id");
                    Integer studentId = rs.wasNull() ? null : sid;
                    return new User(id, user, hash, salt, role, studentId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteByUserId(int userId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1, userId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean usernameExists(String username) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean adminExists() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        String sql = "SELECT 1 FROM users WHERE role = 'ADMIN' LIMIT 1";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()){
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
