import java.util.Scanner;
import java.util.regex.Pattern;

public class Main{
    private static final Pattern VALID_EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        StudentManager studentManager = new StudentManager();
        AuthenticationService authService = new AuthenticationService();
        System.out.println("Welcome to the Student Management System");

        while (true) {
            System.out.println("\n== Main Menu ==");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            int choice = readInt(scanner, "Enter your choice: ");
            switch (choice) {
                case 1:
                    if (DatabaseConnection.getConnection() == null) {
                        System.out.println("Authentication requires a working database connection.\nPlease ensure the MySQL server is running and mysql-connector-java.jar is on the classpath.");
                        break;
                    }
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    char[] password;
                    java.io.Console console = System.console();
                    if (console != null) {
                        password = console.readPassword();
                    } else {
                        // Fallback when running in IDE or piped input
                        password = scanner.nextLine().toCharArray();
                    }
                    User user = authService.authenticate(username, password);
                    if (user == null) {
                        System.out.println("Invalid credentials or authentication failed.");
                        break;
                    }
                    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                        adminMenu(scanner, studentManager, authService, user);
                    } else if ("STUDENT".equalsIgnoreCase(user.getRole())) {
                        studentMenu(scanner, studentManager, user);
                    } else {
                        System.out.println("Unknown role for user.");
                    }
                    break;
                case 2:
                    System.out.println("Goodbye.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void adminMenu(Scanner scanner, StudentManager studentManager, AuthenticationService authService, User currentUser) {
        while (true) {
            System.out.println("\n== Admin Menu ==");
            System.out.println("1. Add Student");
            System.out.println("2. Find Student");
            System.out.println("3. Display All Students");
            System.out.println("4. Update Student");
            System.out.println("5. Remove Student");
            System.out.println("6. Add Course");
            System.out.println("7. Assign Course to Student");
            System.out.println("8. Remove Course from Student");
            System.out.println("9. View Student Courses");
            System.out.println("10. Add/Update Student Grades");
            System.out.println("11. View Student Grades");
            System.out.println("12. Calculate Student Average");
            System.out.println("13. Create Login for Student");
            System.out.println("14. Create Admin Account");
            System.out.println("15. Logout");
            int choice = readInt(scanner, "Enter your choice: ");
            switch (choice) {
                case 1: // Add Student
                    int id = readInt(scanner, "Enter student ID: ");
                    System.out.print("Enter student name: ");
                    String name = scanner.nextLine();
                    if (!isValidName(name)) { System.out.println("Invalid name."); break; }
                    System.out.print("Enter student email: ");
                    String email = scanner.nextLine();
                    if (!isValidEmail(email)) { System.out.println("Invalid email."); break; }
                    int yearLevel = readInt(scanner, "Enter student year level: ");
                    if (!isValidYearLevel(yearLevel)) { System.out.println("Invalid year level."); break; }
                    Student student = new Student(id, name, email, yearLevel);
                    boolean added = studentManager.addStudent(student);
                    System.out.println(added ? "Student added." : "Failed to add student.");
                    break;
                case 2: // Find Student
                    int findId = readInt(scanner, "Enter student ID to find: ");
                    Student found = studentManager.findStudent(findId);
                    if (found == null) System.out.println("Not found."); else {
                        System.out.println("ID: " + found.getId() + " Name: " + found.getName() + " Email: " + found.getEmail());
                    }
                    break;
                case 3:
                    studentManager.displayAllStudents();
                    break;
                case 4: // Update Student
                    int uid = readInt(scanner, "Enter student ID to update: ");
                    Student us = studentManager.findStudent(uid);
                    if (us == null) { System.out.println("Student not found."); break; }
                    System.out.print("New name (leave blank to keep): ");
                    String newName = scanner.nextLine();
                    if (!newName.trim().isEmpty()) us.setName(newName);
                    System.out.print("New email (leave blank to keep): ");
                    String newEmail = scanner.nextLine();
                    if (!newEmail.trim().isEmpty()) {
                        if (!isValidEmail(newEmail)) { System.out.println("Invalid email."); break; }
                        us.setEmail(newEmail);
                    }
                    System.out.print("New year level (0 to keep): ");
                    int newYear = readInt(scanner, "Enter year level (0 to keep): ");
                    if (newYear > 0) us.setYearLevel(newYear);
                    boolean updated = studentManager.updateStudent(us);
                    System.out.println(updated ? "Updated." : "Failed to update.");
                    break;
                case 5: // Remove Student
                    int rid = readInt(scanner, "Enter student ID to remove: ");
                    System.out.print("Are you sure? This will delete enrollments. (y/n): ");
                    String conf = scanner.nextLine();
                    if (conf.equalsIgnoreCase("y")) {
                        boolean rem = studentManager.removeStudent(rid);
                        System.out.println(rem ? "Removed." : "Failed to remove or not found.");
                    } else System.out.println("Cancelled.");
                    break;
                case 6: // Add Course
                    int cId = readInt(scanner, "Enter new course ID: ");
                    System.out.print("Course name: ");
                    String cName = scanner.nextLine();
                    System.out.print("Instructor: ");
                    String instr = scanner.nextLine();
                    Course course = new Course(cId, cName, instr);
                    boolean ok = studentManager.addCourseMaster(course);
                    System.out.println(ok ? "Course added." : "Failed to add course or duplicate.");
                    break;
                case 7: // Assign Course to Student
                    int sid = readInt(scanner, "Enter student ID: ");
                    int assignCid = readInt(scanner, "Enter course ID: ");
                    System.out.print("Course name: ");
                    String assignName = scanner.nextLine();
                    System.out.print("Instructor: ");
                    String assignInstr = scanner.nextLine();
                    Course assignCourse = new Course(assignCid, assignName, assignInstr);
                    boolean assigned = studentManager.addCourseToStudent(sid, assignCourse);
                    System.out.println(assigned ? "Assigned." : "Failed to assign (exists or invalid).");
                    break;
                case 8: // Remove Course from Student
                    int rsid = readInt(scanner, "Enter student ID: ");
                    int rcid = readInt(scanner, "Enter course ID to remove: ");
                    boolean rrem = studentManager.removeCourseFromStudent(rsid, rcid);
                    System.out.println(rrem ? "Enrollment removed." : "Failed to remove enrollment.");
                    break;
                case 9: // View Student Courses
                    int vsid = readInt(scanner, "Enter student ID: ");
                    Student sc = studentManager.findStudent(vsid);
                    if (sc == null) { System.out.println("Not found."); break; }
                    if (sc.getCourses().isEmpty()) { System.out.println("No courses."); break; }
                    for (Course cc : sc.getCourses()) {
                        System.out.println(cc.getCourseId() + " - " + cc.getCourseName() + " Prelim:" + (cc.getPrelim()==null?"N/A":cc.getPrelim()));
                    }
                    break;
                case 10: // Add/Update grades
                    int gsid = readInt(scanner, "Enter student ID: ");
                    int gcid = readInt(scanner, "Enter course ID: ");
                    System.out.println("Which component? 1=Prelim 2=Midterm 3=Final");
                    int comp = readInt(scanner, "Choice: ");
                    double grade = readDouble(scanner, "Enter grade (0-100): ");
                    boolean gres = false;
                    if (comp == 1) gres = studentManager.setPrelimGrade(gsid, gcid, grade);
                    else if (comp == 2) gres = studentManager.setMidtermGrade(gsid, gcid, grade);
                    else if (comp == 3) gres = studentManager.setFinalExamGrade(gsid, gcid, grade);
                    else System.out.println("Invalid component");
                    System.out.println(gres ? "Grade set." : "Failed to set grade.");
                    break;
                case 11: // View Student Grades
                    int vgid = readInt(scanner, "Enter student ID: ");
                    Student vs = studentManager.findStudent(vgid);
                    if (vs == null) { System.out.println("Not found."); break; }
                    for (Course cc : vs.getCourses()) {
                        System.out.println("Course: " + cc.getCourseId() + " Prelim:" + (cc.getPrelim()==null?"N/A":cc.getPrelim()) + " Mid:" + (cc.getMidterm()==null?"N/A":cc.getMidterm()) + " Final:" + (cc.getFinalExam()==null?"N/A":cc.getFinalExam()) + " FinalGrade:" + (cc.getCourseFinalGrade()==null?"N/A":cc.getCourseFinalGrade()));
                    }
                    break;
                case 12: // Calculate Student Average
                    int avgId = readInt(scanner, "Enter student ID: ");
                    java.util.OptionalDouble averageOpt = studentManager.calculateAverageGrade(avgId);
                    if(averageOpt.isPresent()) System.out.println("Average: " + averageOpt.getAsDouble()); else System.out.println("No grades or not found.");
                    break;
                case 13: // Create Login for Student
                    if (DatabaseConnection.getConnection() == null) {
                        System.out.println("Cannot create user: database connection unavailable.");
                        break;
                    }
                    System.out.print("Enter new username for student: ");
                    String suser = scanner.nextLine().trim();
                    System.out.print("Enter password: ");
                    char[] spass;
                    java.io.Console cons = System.console();
                    if (cons != null) spass = cons.readPassword(); else spass = scanner.nextLine().toCharArray();
                    int linkedId = readInt(scanner, "Enter student ID to link this account to: ");
                    if (studentManager.findStudent(linkedId) == null) {
                        System.out.println("Student ID not found.");
                        break;
                    }
                    boolean createdStudentLogin = authService.registerUser(suser, spass, "STUDENT", linkedId);
                    System.out.println(createdStudentLogin ? "Student login created." : "Failed to create student login (duplicate username or DB issue).");
                    break;
                case 14: // Create Admin Account
                    if (DatabaseConnection.getConnection() == null) {
                        System.out.println("Cannot create admin: database connection unavailable.");
                        break;
                    }
                    System.out.print("Enter new admin username: ");
                    String auser = scanner.nextLine().trim();
                    System.out.print("Enter password: ");
                    char[] apass;
                    java.io.Console cons2 = System.console();
                    if (cons2 != null) apass = cons2.readPassword(); else apass = scanner.nextLine().toCharArray();
                    boolean createdAdmin = authService.registerUser(auser, apass, "ADMIN", null);
                    System.out.println(createdAdmin ? "Admin account created." : "Failed to create admin (duplicate username or DB issue).");
                    break;
                case 15:
                    System.out.println("Logging out.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void studentMenu(Scanner scanner, StudentManager studentManager, User currentUser) {
        Integer sid = currentUser.getStudentId();
        if (sid == null) {
            System.out.println("No student record linked to this account.");
            return;
        }
        while (true) {
            System.out.println("\n== Student Menu ==");
            System.out.println("1. View Profile");
            System.out.println("2. View My Courses");
            System.out.println("3. View My Grades");
            System.out.println("4. View My Average");
            System.out.println("5. Logout");
            int choice = readInt(scanner, "Enter your choice: ");
            switch (choice) {
                case 1:
                    Student s = studentManager.findStudent(sid);
                    if (s == null) System.out.println("Student record not found."); else System.out.println("ID:"+s.getId()+" Name:"+s.getName()+" Email:"+s.getEmail());
                    break;
                case 2:
                    Student s2 = studentManager.findStudent(sid);
                    if (s2 == null) { System.out.println("Not found."); break; }
                    for (Course c : s2.getCourses()) System.out.println(c.getCourseId()+" - "+c.getCourseName());
                    break;
                case 3:
                    Student s3 = studentManager.findStudent(sid);
                    if (s3 == null) { System.out.println("Not found."); break; }
                    for (Course c : s3.getCourses()) System.out.println(c.getCourseName()+" Prelim:"+(c.getPrelim()==null?"N/A":c.getPrelim())+" Mid:"+(c.getMidterm()==null?"N/A":c.getMidterm())+" Final:"+(c.getFinalExam()==null?"N/A":c.getFinalExam())+" FinalGrade:"+(c.getCourseFinalGrade()==null?"N/A":c.getCourseFinalGrade()));
                    break;
                case 4:
                    java.util.OptionalDouble avg = studentManager.calculateAverageGrade(sid);
                    if (avg.isPresent()) System.out.println("Your average: " + avg.getAsDouble()); else System.out.println("No grades yet.");
                    break;
                case 5:
                    System.out.println("Logging out.");
                    return;
                default:
                    System.out.println("Invalid choice.");
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

    private static boolean isValidName(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static boolean isValidEmail(String value) {
        return value != null && !value.trim().isEmpty() && VALID_EMAIL_PATTERN.matcher(value.trim()).matches();
    }

    private static boolean isValidYearLevel(int value) {
        return value > 0;
    }
}



