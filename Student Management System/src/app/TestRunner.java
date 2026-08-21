package app;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.OptionalDouble;

import manager.StudentManager;
import model.Student;
import model.Course;
import java.io.PrintStream;
import java.util.List;
import java.util.OptionalDouble;

public class TestRunner {
    static int total = 0;
    static int passed = 0;

    public static void main(String[] args) {
        runAllTests();
        System.out.println("\nFINAL SUMMARY: " + passed + " / " + total + " passed.");
    }

    private static void runAllTests() {
        testAddStudentNormal();
        testAddStudentDuplicate();
        testAddStudentInvalidName();
        testAddStudentInvalidEmail();
        testAddStudentInvalidEmailPattern();
        testAddStudentInvalidYear();
        testStudentEqualityById();
        testFindStudentNotFound();
        testDisplayAllStudentsEmpty();
        testDisplayAllStudentsNonEmpty();
        testRemoveStudentExisting();
        testRemoveStudentNonExisting();
        testAddGradeToStudentNormal();
        testAddGradeToStudentMissing();
        testCalculateAverageMultipleGrades();
        testCalculateAverageNoGrades();
        testAddCourseToStudentNormal();
        testAddCourseToStudentMissing();
        testAddDuplicateCourseRejected();
        testViewCoursesEmpty();
        testReadOnlyListProtection();
        testInteractionFlow();
    }

    private static void assertTrue(boolean cond, String name) {
        total++;
        if (cond) { passed++; System.out.println("PASS: " + name); }
        else { System.out.println("FAIL: " + name); }
    }

    // Tests
    private static void testAddStudentNormal() {
        StudentManager mgr = new StudentManager();
        boolean ok = mgr.addStudent(new Student(10, "T1", "t1@x.com", 1));
        assertTrue(ok, "Add student normal (returns true)");
        Student s = mgr.findStudent(10);
        assertTrue(s != null && "T1".equals(s.getName()), "Added student retrievable and name matches");
    }

    private static void testAddStudentDuplicate() {
        StudentManager mgr = new StudentManager();
        boolean a1 = mgr.addStudent(new Student(11, "A", "a@x.com", 2));
        boolean a2 = mgr.addStudent(new Student(11, "B", "b@x.com", 3));
        assertTrue(a1 && !a2, "Duplicate ID rejected");
    }

    private static void testAddStudentInvalidName() {
        StudentManager mgr = new StudentManager();
        boolean ok = mgr.addStudent(new Student(12, "   ", "n@x.com", 1));
        assertTrue(!ok, "Blank student name rejected");
    }

    private static void testAddStudentInvalidEmail() {
        StudentManager mgr = new StudentManager();
        boolean ok = mgr.addStudent(new Student(13, "N", "bad-email", 1));
        assertTrue(!ok, "Email missing '@' rejected");
    }

    private static void testAddStudentInvalidEmailPattern() {
        StudentManager mgr = new StudentManager();
        boolean invalid1 = mgr.addStudent(new Student(15, "N", "name@domain", 1));
        boolean invalid2 = mgr.addStudent(new Student(16, "N", "name@.com", 1));
        assertTrue(!invalid1 && !invalid2, "Malformed email patterns rejected");
    }

    private static void testAddStudentInvalidYear() {
        StudentManager mgr = new StudentManager();
        boolean ok = mgr.addStudent(new Student(14, "N", "n@x.com", 0));
        assertTrue(!ok, "Non-positive year level rejected");
    }

    private static void testStudentEqualityById() {
        Student a = new Student(17, "Match", "m@x.com", 2);
        Student b = new Student(17, "Other", "o@x.com", 3);
        assertTrue(a.equals(b) && a.hashCode() == b.hashCode(), "Students with same ID are equal by ID");
    }

    private static void testFindStudentNotFound() {
        StudentManager mgr = new StudentManager();
        Student s = mgr.findStudent(9999);
        assertTrue(s == null, "findStudent returns null for nonexistent ID");
    }

    private static void testDisplayAllStudentsEmpty() {
        StudentManager mgr = new StudentManager();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(baos));
        mgr.displayAllStudents();
        System.out.flush();
        System.setOut(old);
        String out = baos.toString();
        assertTrue(out.contains("No students found"), "displayAllStudents prints 'No students found' when empty");
    }

    private static void testDisplayAllStudentsNonEmpty() {
        StudentManager mgr = new StudentManager();
        mgr.addStudent(new Student(20, "S2", "s2@x.com", 2));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(baos));
        mgr.displayAllStudents();
        System.out.flush();
        System.setOut(old);
        String out = baos.toString();
        assertTrue(out.contains("Student ID: 20") && out.contains("Name: S2"), "displayAllStudents prints student details for non-empty list");
    }

    private static void testRemoveStudentExisting() {
        StudentManager mgr = new StudentManager();
        mgr.addStudent(new Student(30, "Rem", "r@x.com", 3));
        boolean removed = mgr.removeStudent(30);
        assertTrue(removed, "removeStudent returns true for existing student");
        assertTrue(mgr.findStudent(30) == null, "Student no longer retrievable after removal");
    }

    private static void testRemoveStudentNonExisting() {
        StudentManager mgr = new StudentManager();
        boolean removed = mgr.removeStudent(404);
        assertTrue(!removed, "removeStudent returns false for nonexistent student");
    }

    private static void testAddGradeToStudentNormal() {
        StudentManager mgr = new StudentManager();
        mgr.addStudent(new Student(40, "G1", "g1@x.com", 1));
        // add a course first
        mgr.addCourseToStudent(40, new Course(400, "Math", "DrM"));
        // set all components so course final grade is computed
        boolean r1 = mgr.setPrelimGrade(40, 400, 75.5);
        boolean r2 = mgr.setMidtermGrade(40, 400, 75.5);
        boolean r3 = mgr.setFinalExamGrade(40, 400, 75.5);
        assertTrue(r1 && r2 && r3, "Setting all components returns true");
        OptionalDouble avg = mgr.calculateAverageGrade(40);
        assertTrue(avg.isPresent() && Math.abs(avg.getAsDouble() - 75.5) < 1e-6, "calculateAverageGrade returns correct average after one graded course");
    }

    private static void testAddGradeToStudentMissing() {
        StudentManager mgr = new StudentManager();
        boolean res = mgr.setPrelimGrade(9998, 1, 60.0);
        assertTrue(!res, "setPrelimGrade returns false for missing student");
    }

    private static void testCalculateAverageMultipleGrades() {
        StudentManager mgr = new StudentManager();
        mgr.addStudent(new Student(50, "Avg", "a@x.com", 2));
        mgr.addCourseToStudent(50, new Course(501, "C1", "I1"));
        mgr.addCourseToStudent(50, new Course(502, "C2", "I2"));
        mgr.addCourseToStudent(50, new Course(503, "C3", "I3"));
        mgr.setPrelimGrade(50, 501, 80.0);
        mgr.setMidtermGrade(50, 501, 80.0);
        mgr.setFinalExamGrade(50, 501, 80.0);
        mgr.setPrelimGrade(50, 502, 90.0);
        mgr.setMidtermGrade(50, 502, 90.0);
        mgr.setFinalExamGrade(50, 502, 90.0);
        mgr.setPrelimGrade(50, 503, 70.0);
        mgr.setMidtermGrade(50, 503, 70.0);
        mgr.setFinalExamGrade(50, 503, 70.0);
        OptionalDouble avg = mgr.calculateAverageGrade(50);
        assertTrue(avg.isPresent() && Math.abs(avg.getAsDouble() - 80.0) < 1e-6, "calculateAverageGrade correct for multiple graded courses");
    }

    private static void testCalculateAverageNoGrades() {
        StudentManager mgr = new StudentManager();
        mgr.addStudent(new Student(60, "NoG", "n@x.com", 1));
        // student has no courses and therefore no grades
        OptionalDouble avg = mgr.calculateAverageGrade(60);
        assertTrue(!avg.isPresent(), "calculateAverageGrade returns empty when no grades available");
    }

    private static void testAddCourseToStudentNormal() {
        StudentManager mgr = new StudentManager();
        mgr.addStudent(new Student(70, "C1", "c1@x.com", 1));
        boolean r = mgr.addCourseToStudent(70, new Course(301, "Chem", "DrC"));
        assertTrue(r, "addCourseToStudent returns true for existing student");
        List<Course> courses = mgr.getCoursesOfStudent(70);
        assertTrue(courses != null && courses.size() == 1 && courses.get(0).getCourseId() == 301, "Course added and retrievable with correct ID");
    }

    private static void testAddCourseToStudentMissing() {
        StudentManager mgr = new StudentManager();
        boolean r = mgr.addCourseToStudent(9997, new Course(302, "Bio", "DrB"));
        assertTrue(!r, "addCourseToStudent returns false for missing student");
    }

    private static void testAddDuplicateCourseRejected() {
        StudentManager mgr = new StudentManager();
        mgr.addStudent(new Student(81, "DupCourse", "d@x.com", 2));
        boolean first = mgr.addCourseToStudent(81, new Course(501, "History", "DrH"));
        boolean second = mgr.addCourseToStudent(81, new Course(501, "History Again", "DrH2"));
        assertTrue(first && !second, "Duplicate course ID for same student rejected");
    }

    private static void testViewCoursesEmpty() {
        StudentManager mgr = new StudentManager();
        mgr.addStudent(new Student(80, "EmptyC", "e@x.com", 2));
        List<Course> courses = mgr.getCoursesOfStudent(80);
        assertTrue(courses != null && courses.isEmpty(), "getCoursesOfStudent returns empty list when no courses");
    }

    private static void testReadOnlyListProtection() {
        Student student = new Student(90, "ReadOnly", "ro@x.com", 2);
        boolean courseFail = false;
        try {
            student.getCourses().add(new Course(900, "Course", "Instr"));
        } catch (UnsupportedOperationException e) {
            courseFail = true;
        }
        assertTrue(courseFail, "Student course list is read-only from external access");
    }

    private static void testInteractionFlow() {
        StudentManager mgr = new StudentManager();
        mgr.addStudent(new Student(90, "Flow", "f@x.com", 3));
        mgr.addCourseToStudent(90, new Course(401, "Eng", "DrE"));
        mgr.setPrelimGrade(90, 401, 88.0);
        mgr.setMidtermGrade(90, 401, 88.0);
        mgr.setFinalExamGrade(90, 401, 88.0);

        Student s = mgr.findStudent(90);
        assertTrue(s != null && s.getName().equals("Flow"), "Interaction: added student found");
        OptionalDouble avg = mgr.calculateAverageGrade(90);
        assertTrue(avg.isPresent() && Math.abs(avg.getAsDouble() - 88.0) < 1e-6, "Interaction: grade present and average correct");
        List<Course> courses = mgr.getCoursesOfStudent(90);
        assertTrue(courses != null && courses.size() == 1 && courses.get(0).getCourseName().equals("Eng"), "Interaction: course added and viewable");

        boolean removed = mgr.removeStudent(90);
        assertTrue(removed, "Interaction: student removal returns true");
        assertTrue(mgr.findStudent(90) == null, "Interaction: removed student no longer found");
    }
}