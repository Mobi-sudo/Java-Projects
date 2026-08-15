import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Student {
    private int id;
    private String name;
    private String email;
    private int yearLevel;
    private List<Course> courses;

    public Student(int id, String name, String email, int yearLevel){
        this.id = id;
        this.name = name;
        this.email = email;
        this.yearLevel = yearLevel;
        this.courses = new ArrayList<>();
    }
    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public int getYearLevel(){
        return yearLevel;
    }

    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    public void addCourse(Course course) {
        courses.add(course);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return id == student.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
