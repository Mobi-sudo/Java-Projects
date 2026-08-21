package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Student;
import model.Course;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    public boolean saveStudent(Student s) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        String sql = "INSERT INTO students (id, name, email, year_level) VALUES (?, ?, ?, ?)";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getId());
            ps.setString(2, s.getName());
            ps.setString(3, s.getEmail());
            ps.setInt(4, s.getYearLevel());
            int rows = ps.executeUpdate();
            return rows == 1;
        } catch (SQLIntegrityConstraintViolationException e) {
            // duplicate primary key or unique email
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Student findStudent(int id) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;
        String sql = "SELECT id, name, email, year_level FROM students WHERE id = ?";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()){
                    return null;
                }
                Student s = new Student(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getInt("year_level"));
                // load courses
                String csql = "SELECT c.course_id, c.course_name, c.instructor, e.prelim, e.midterm, e.final FROM courses c JOIN enrollments e ON c.course_id = e.course_id WHERE e.student_id = ?";
                try (PreparedStatement cps = c.prepareStatement(csql)){
                    cps.setInt(1, id);
                    try (ResultSet crs = cps.executeQuery()){
                        while (crs.next()){
                            Course course = new Course(crs.getInt("course_id"), crs.getString("course_name"), crs.getString("instructor"));
                            Double prelim = crs.getObject("prelim") == null ? null : crs.getDouble("prelim");
                            Double mid = crs.getObject("midterm") == null ? null : crs.getDouble("midterm");
                            Double fin = crs.getObject("final") == null ? null : crs.getDouble("final");
                            if (prelim != null) course.setPrelim(prelim);
                            if (mid != null) course.setMidterm(mid);
                            if (fin != null) course.setFinalExam(fin);
                            s.addCourse(course);
                        }
                    }
                }
                return s;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteStudent(int id) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Student> getAllStudents() {
        List<Student> out = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return out;
        String sql = "SELECT id, name, email, year_level FROM students";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()){
            while (rs.next()){
                Student s = new Student(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getInt("year_level"));
                out.add(s);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return out;
    }

    public boolean updateStudent(Student s) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        String sql = "UPDATE students SET name = ?, email = ?, year_level = ? WHERE id = ?";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setInt(3, s.getYearLevel());
            ps.setInt(4, s.getId());
            int rows = ps.executeUpdate();
            return rows == 1;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExists(String email){
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        String sql = "SELECT 1 FROM students WHERE email = ? LIMIT 1";
        try (Connection c = conn; PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
