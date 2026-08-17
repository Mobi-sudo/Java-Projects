import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {
    // Ensure course exists in master list
    public boolean ensureCourseExists(Course c) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        String sql = "INSERT IGNORE INTO courses (course_id, course_name, instructor) VALUES (?, ?, ?)";
        try (Connection cn = conn; PreparedStatement ps = cn.prepareStatement(sql)){
            ps.setInt(1, c.getCourseId());
            ps.setString(2, c.getCourseName());
            ps.setString(3, c.getInstructor());
            ps.executeUpdate();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    // Add enrollment row linking student and course
    public boolean addCourseToStudent(int studentId, Course c) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        // ensure master course exists
        ensureCourseExists(c);
        String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
        try (Connection cn = conn; PreparedStatement ps = cn.prepareStatement(sql)){
            ps.setInt(1, studentId);
            ps.setInt(2, c.getCourseId());
            int rows = ps.executeUpdate();
            return rows == 1;
        } catch (SQLIntegrityConstraintViolationException e){
            // duplicate enrollment or FK violation
            return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Course> getCoursesForStudent(int studentId){
        List<Course> out = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return out;
        String sql = "SELECT c.course_id, c.course_name, c.instructor, e.prelim, e.midterm, e.final FROM courses c JOIN enrollments e ON c.course_id = e.course_id WHERE e.student_id = ?";
        try (Connection cn = conn; PreparedStatement ps = cn.prepareStatement(sql)){
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    Course c = new Course(rs.getInt("course_id"), rs.getString("course_name"), rs.getString("instructor"));
                    Double prelim = rs.getObject("prelim") == null ? null : rs.getDouble("prelim");
                    Double mid = rs.getObject("midterm") == null ? null : rs.getDouble("midterm");
                    Double fin = rs.getObject("final") == null ? null : rs.getDouble("final");
                    if (prelim != null) c.setPrelim(prelim);
                    if (mid != null) c.setMidterm(mid);
                    if (fin != null) c.setFinalExam(fin);
                    out.add(c);
                }
            }
            return out;
        } catch (Exception e){
            e.printStackTrace();
            return out;
        }
    }

    public boolean removeCourseFromStudent(int studentId, int courseId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection cn = conn; PreparedStatement ps = cn.prepareStatement(sql)){
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
