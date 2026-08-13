import java.util.ArrayList;
import java.util.List;

public class Student {
    private int id;
    private String name;
    private String email;
    private int yearLevel;
    private List<Double> grades;
    private List<Course> courses;

    public Student(int id, String name, String email, int yearLevel){
        this.id = id;
        this.name = name;
        this.email = email;
        this.yearLevel = yearLevel;
        this.grades = new ArrayList<>();
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

    public List<Double> getGrades() {
        return grades;
    }

    public List<Course> getCourses() {
        return courses;
    }
}
