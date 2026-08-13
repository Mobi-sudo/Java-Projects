import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.regex.Pattern;

public class StudentManager {
    private static final Pattern VALID_EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private List<Student> students = new ArrayList<>();

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
        students.add(student);
        return true;
    }

    public Student findStudent(int id){
        for(Student student : students){
            if(student.getId() == id){
                return student;
            }
        }
        return null;
    }

    public void displayAllStudents() {
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
        Student student = findStudent(id);
        if(student != null){
            students.remove(student);
            return true;
        }
        return false;
    }

    // Returns true on success; false if student not found or grade invalid
    public boolean addGradeToStudent(int studentId, double grade){
        if (grade < 0.0 || grade > 100.0) return false;
        Student student = findStudent(studentId);
        if(student != null){
            student.addGrade(grade);
            return true;
        }
        return false;
    }

    // Returns average wrapped in OptionalDouble; empty if student not found or no grades
    public OptionalDouble calculateAverageGrade(int studentId){
        Student student = findStudent(studentId);
        if(student != null && !student.getGrades().isEmpty()){
            double sum = 0;
            for(double grade : student.getGrades()){
                sum += grade;
            }
            return OptionalDouble.of(sum / student.getGrades().size());
        }
        return OptionalDouble.empty();
    }

    // Returns true on success; false if student not found or course invalid
    public boolean addCourseToStudent(int studentId, Course course){
        if (course == null) return false;
        if (course.getCourseId() <= 0) return false;
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) return false;
        if (course.getInstructor() == null || course.getInstructor().trim().isEmpty()) return false;
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