import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.regex.Pattern;

public class StudentManager {
    private static final Pattern VALID_EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private List<Student> students = new ArrayList<>();
    // DAOs for optional DB-backed persistence
    private final boolean useDb;
    private final StudentDAO studentDAO;
    private final CourseDAO courseDAO;
    private final EnrollmentDAO enrollmentDAO;

    // Constructor detects DB availability and initializes DAOs
    public StudentManager() {
        Connection testConn = DatabaseConnection.getConnection();
        boolean dbAvailable = false;
        if (testConn != null) {
            dbAvailable = true;
            try { testConn.close(); } catch (Exception e) { }
        }
        this.useDb = dbAvailable;
        this.studentDAO = new StudentDAO();
        this.courseDAO = new CourseDAO();
        this.enrollmentDAO = new EnrollmentDAO();
    }

    // Return true if added, false if invalid or a student with same ID exists
    public boolean addStudent(Student student) {
        if (student == null) return false;
        if (student.getId() <= 0) return false;
        if (student.getName() == null || student.getName().trim().isEmpty()) return false;
        if (!isValidEmail(student.getEmail())) return false;
        if (student.getYearLevel() <= 0) return false;
        if (findStudent(student.getId()) != null) {
            return false;
        }
        if (useDb) {
            // check duplicate email in DB
            if (studentDAO.emailExists(student.getEmail())) return false;
            return studentDAO.saveStudent(student);
        }
        students.add(student);
        return true;
    }

    public Student findStudent(int id){
        if (useDb) {
            return studentDAO.findStudent(id);
        }
        for(Student student : students){
            if(student.getId() == id){
                return student;
            }
        }
        return null;
    }

    public void displayAllStudents() {
        if (useDb) {
            List<Student> all = studentDAO.getAllStudents();
            if (all.isEmpty()) {
                System.out.println("No students found.");
                return;
            }
            for (Student student : all) {
                System.out.println("Student ID: "+ student.getId());
                System.out.println("Name: " + student.getName());
                System.out.println("Email: " + student.getEmail());
                System.out.println("Year Level: " + student.getYearLevel());
                System.out.println("----------------------------");
            }
            return;
        }
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        for (Student student : students) {
            System.out.println("Student ID: "+ student.getId());
            System.out.println("Name: " + student.getName());
            System.out.println("Email: " + student.getEmail());
            System.out.println("Year Level: " + student.getYearLevel());
            System.out.println("----------------------------");
        }
    }

    public boolean removeStudent(int id){
        if (useDb) {
            return studentDAO.deleteStudent(id);
        }
        Student student = findStudent(id);
        if(student != null){
            students.remove(student);
            return true;
        }
        return false;
    }

    // Set prelim grade for a student's course
    public boolean setPrelimGrade(int studentId, int courseId, double grade){
        if (grade < 0.0 || grade > 100.0) return false;
        if (useDb) return enrollmentDAO.setPrelim(studentId, courseId, grade);
        Student student = findStudent(studentId);
        if (student == null) return false;
        for (Course c : student.getCourses()){
            if (c.getCourseId() == courseId){
                c.setPrelim(grade);
                return true;
            }
        }
        return false;
    }

    // Set midterm grade for a student's course
    public boolean setMidtermGrade(int studentId, int courseId, double grade){
        if (grade < 0.0 || grade > 100.0) return false;
        if (useDb) return enrollmentDAO.setMidterm(studentId, courseId, grade);
        Student student = findStudent(studentId);
        if (student == null) return false;
        for (Course c : student.getCourses()){
            if (c.getCourseId() == courseId){
                c.setMidterm(grade);
                return true;
            }
        }
        return false;
    }

    // Set final exam grade for a student's course
    public boolean setFinalExamGrade(int studentId, int courseId, double grade){
        if (grade < 0.0 || grade > 100.0) return false;
        if (useDb) return enrollmentDAO.setFinal(studentId, courseId, grade);
        Student student = findStudent(studentId);
        if (student == null) return false;
        for (Course c : student.getCourses()){
            if (c.getCourseId() == courseId){
                c.setFinalExam(grade);
                return true;
            }
        }
        return false;
    }

    // Returns average wrapped in OptionalDouble; empty if student not found or no courses with a final grade
    public OptionalDouble calculateAverageGrade(int studentId){
        if (useDb) return enrollmentDAO.getStudentAverage(studentId);
        Student student = findStudent(studentId);
        if (student == null) return OptionalDouble.empty();
        double sum = 0;
        int count = 0;
        for (Course c : student.getCourses()){
            Double finalG = c.getCourseFinalGrade();
            if (finalG != null){
                sum += finalG;
                count++;
            }
        }
        if (count == 0) return OptionalDouble.empty();
        return OptionalDouble.of(sum / count);
    }

    // Returns true on success; false if student not found or course invalid
    public boolean addCourseToStudent(int studentId, Course course){
        if (course == null) return false;
        if (course.getCourseId() <= 0) return false;
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) return false;
        if (course.getInstructor() == null || course.getInstructor().trim().isEmpty()) return false;
        if (useDb) {
            // Use DAO to add; DAO will ensure master course exists and add enrollment row
            return courseDAO.addCourseToStudent(studentId, course);
        }
        Student student = findStudent(studentId);
        if(student != null){
            for (Course existing : student.getCourses()) {
                if (existing.getCourseId() == course.getCourseId()) {
                    return false;
                }
            }
            student.addCourse(course);
            return true;
        }
        return false;
    }

    // Returns an immutable list (empty if student not found or no courses)
    public List<Course> getCoursesOfStudent(int studentId){
        if (useDb) {
            return Collections.unmodifiableList(courseDAO.getCoursesForStudent(studentId));
        }
        Student student = findStudent(studentId);
        if(student != null){
            return Collections.unmodifiableList(student.getCourses());
        }
        return Collections.emptyList();
    }

    private static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && VALID_EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}