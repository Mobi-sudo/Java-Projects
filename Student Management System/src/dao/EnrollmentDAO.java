package dao;

import java.sql.*;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.HashSet;

public class EnrollmentDAO {
    private static final Set<String> ALLOWED_COLUMNS = new HashSet<>();
    static {
        ALLOWED_COLUMNS.add("prelim");
        ALLOWED_COLUMNS.add("midterm");
        ALLOWED_COLUMNS.add("final");
    }

    public boolean setPrelim(int studentId, int courseId, double grade){
        return setComponent(studentId, courseId, "prelim", grade);
    }
    public boolean setMidterm(int studentId, int courseId, double grade){
        return setComponent(studentId, courseId, "midterm", grade);
    }
    public boolean setFinal(int studentId, int courseId, double grade){
        return setComponent(studentId, courseId, "final", grade);
    }

    private boolean setComponent(int studentId, int courseId, String column, double grade){
        if (grade < 0.0 || grade > 100.0) return false;
        if (!ALLOWED_COLUMNS.contains(column)) return false;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        // Use backticks around column name to avoid reserved-word issues; column validated against ALLOWED_COLUMNS
        String sql = "UPDATE enrollments SET `" + column + "` = ? WHERE student_id = ? AND course_id = ?";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql)){
            ps.setDouble(1, grade);
            ps.setInt(2, studentId);
            ps.setInt(3, courseId);
            int rows = ps.executeUpdate();
            return rows == 1;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public OptionalDouble getStudentAverage(int studentId){
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return OptionalDouble.empty();
        String sql = "SELECT AVG(final_grade) as avgg FROM enrollments WHERE student_id = ? AND final_grade IS NOT NULL";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    double val = rs.getDouble("avgg");
                    if (rs.wasNull()){
                        return OptionalDouble.empty();
                    }
                    return OptionalDouble.of(val);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return OptionalDouble.empty();
    }
}
