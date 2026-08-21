package app;

import java.util.Scanner;
import service.AuthenticationService;
import dao.UserDAO;
import dao.DatabaseConnection;

public class SetupAdmin {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        // Check DB availability
        if (DatabaseConnection.getConnection() == null) {
            System.out.println("Database unavailable. Ensure MySQL is running and connector is on the classpath.");
            return;
        }
        if (userDAO.adminExists()) {
            System.out.println("An admin account already exists. Setup aborted.");
            return;
        }
        AuthenticationService auth = new AuthenticationService();
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Initial Admin Setup ===");
        System.out.print("Enter admin username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty");
            return;
        }
        System.out.print("Enter admin password: ");
        char[] password;
        java.io.Console cons = System.console();
        if (cons != null) password = cons.readPassword(); else password = scanner.nextLine().toCharArray();
        if (password.length == 0) {
            System.out.println("Password cannot be empty");
            return;
        }
        boolean ok = auth.registerUser(username, password, "ADMIN", null);
        if (ok) System.out.println("Admin user created. You may now login with this user.");
        else System.out.println("Failed to create admin (username may already exist or DB unavailable).");
    }
}
