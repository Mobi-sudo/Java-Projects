
package model;

public class Course {
    private int courseId;
    private String courseName;
    private String instructor;
    // Component grades (nullable). Null means not yet assigned.
    private Double prelim;
    private Double midterm;
    private Double finalExam;

    public Course(int courseId, String courseName, String instructor){
        this.courseId = courseId;
        this.courseName = courseName;
        this.instructor = instructor;
        this.prelim = null;
        this.midterm = null;
        this.finalExam = null;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getInstructor() {
        return instructor;
    }

    public Double getPrelim() {
        return prelim;
    }

    public void setPrelim(Double prelim) {
        this.prelim = prelim;
    }

    public Double getMidterm() {
        return midterm;
    }

    public void setMidterm(Double midterm) {
        this.midterm = midterm;
    }

    public Double getFinalExam() {
        return finalExam;
    }

    public void setFinalExam(Double finalExam) {
        this.finalExam = finalExam;
    }

    // Returns the course final grade (average of prelim, midterm, finalExam) or null if any component missing
    public Double getCourseFinalGrade() {
        if (prelim == null || midterm == null || finalExam == null) return null;
        double avg = (prelim + midterm + finalExam) / 3.0;
        // Round to 2 decimal places
        return Math.round(avg * 100.0) / 100.0;
    }
}