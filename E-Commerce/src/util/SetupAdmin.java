package util;

import dao.UserDAO;
import model.AccountStatus;
import model.User;
import model.UserRole;

public class SetupAdmin {
    public static void ensureAdminExists(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin email is required.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Admin password must be at least 8 characters.");
        }
        try {
            UserDAO dao = new UserDAO();
            User existing = dao.findByEmail(email.trim().toLowerCase());
            if (existing != null) {
                System.out.println("Admin user already exists: " + email);
                return;
            }

            String hash = PasswordUtils.hashPassword(password);
            User admin = new User("Admin", email.trim().toLowerCase(), hash, UserRole.ADMIN);
            admin.setStatus(AccountStatus.ACTIVE);
            dao.createUser(admin);
            System.out.println("Created admin user: " + email + " id=" + admin.getId());
        } catch (Exception e) {
            System.err.println("Failed to create admin: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: SetupAdmin <email> <password>");
            return;
        }
        ensureAdminExists(args[0], args[1]);
    }
}
