import java.util.Scanner;

public class Main{
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        StudentManager studentManager = new StudentManager();
        System.out.println("Welcome to the Student Management System");

        while (true){
            System.out.println("1. Add Student");
            System.out.println("2. Find Student");
            System.out.println("3. Display All Students");
            System.out.println("4. Remove Student");
            System.out.println("5. Add Grade to Student");
            System.out.println("6. Calculate Average Grade");
            System.out.println("7. Add Course to Student");
            System.out.println("8. View Courses of Student");
            System.out.println("9. Exit");
            int choice = readInt(scanner, "Enter your choice: ");

            switch(choice){
                case 1:
                    int id = readInt(scanner, "Enter student ID: ");
                    System.out.print("Enter student name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter student email: ");
                    String email = scanner.nextLine();
                    int yearLevel = readInt(scanner, "Enter student year level: ");
                    Student student = new Student(id, name, email, yearLevel);
                    boolean added = studentManager.addStudent(student);
                    if (added) {
                        System.out.println("Student added successfully!");
                    } else {
                        System.out.println("Student with ID " + id + " already exists.");
                    }
                    break;
                case 2:
                    int findId = readInt(scanner, "Enter student ID to find: ");
                    Student foundStudent = studentManager.findStudent(findId);
                    if(foundStudent != null){
                        System.out.println("== Student Details ==");
                        System.out.println("ID: " + foundStudent.getId());
                        System.out.println("Name: " + foundStudent.getName());
                        System.out.println("Email: " + foundStudent.getEmail());
                        System.out.println("Year Level: " + foundStudent.getYearLevel());
                        System.out.println("----------------------------");
                    } else {
                        System.out.println("Student not found.");
                    }
                    break;
                case 3:
                    studentManager.displayAllStudents();
                    break;
                case 4:
                    int removeId = readInt(scanner, "Enter student ID to remove: ");
                    boolean removed = studentManager.removeStudent(removeId);
                    if(removed){
                        System.out.println("Student removed successfully.");
                    } else {
                        System.out.println("Student not found.");
                    }
                    break;
                case 5:
                    int gradeId = readInt(scanner, "Enter student ID to add grade: ");
                    double grade = readDouble(scanner, "Enter grade (0-100): ");
                    boolean gradeAdded = studentManager.addGradeToStudent(gradeId, grade);
                    if(gradeAdded){
                        System.out.println("Grade added successfully.");
                    } else {
                        System.out.println("Student not found or invalid grade.");
                    }
                    break;
                case 6:
                    int avgId = readInt(scanner, "Enter student ID to calculate average grade: ");
                    java.util.OptionalDouble averageOpt = studentManager.calculateAverageGrade(avgId);
                    if(averageOpt.isPresent()){
                        System.out.println("Average Grade: " + averageOpt.getAsDouble());
                    } else {
                        System.out.println("Student not found or no grades available.");
                    }
                    break;
                case 7:
                    int courseId = readInt(scanner, "Enter student ID to add course: ");
                    int cId = readInt(scanner, "Enter course ID: ");
                    System.out.print("Enter course name: ");
                    String cName = scanner.nextLine();
                    System.out.print("Enter instructor name: ");
                    String instructor = scanner.nextLine();
                    Course course = new Course(cId, cName, instructor);
                    boolean courseResult = studentManager.addCourseToStudent(courseId, course);
                    if(courseResult){
                        System.out.println("Course added successfully.");
                    } else {
                        System.out.println("Student not found or invalid course.");
                    }
                    break;
                case 8:
                    int viewId = readInt(scanner, "Enter student ID to view courses: ");
                    Student studentWithCourses = studentManager.findStudent(viewId);
                    if(studentWithCourses == null){
                        System.out.println("Student not found.");
                    } else if (studentWithCourses.getCourses().isEmpty()) {
                        System.out.println("Student found but has no courses.");
                    } else {
                        System.out.println("== Courses for Student ID: " + viewId + " ==");
                        for(Course c : studentWithCourses.getCourses()){
                            System.out.println("Course ID: " + c.getCourseId());
                            System.out.println("Course Name: " + c.getCourseName());
                            System.out.println("Instructor: " + c.getInstructor());
                            System.out.println("----------------------------");
                        }
                    }
                    break;
                case 9:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer.");
            }
        }
    }

    private static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                double val = Double.parseDouble(line.trim());
                if (val < 0 || val > 100) {
                    System.out.println("Please enter a grade between 0 and 100.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}
