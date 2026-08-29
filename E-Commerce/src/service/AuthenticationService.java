package service;

import dao.UserDAO;
import model.User;
import model.UserRole;
import util.PasswordUtils;

import java.util.regex.Pattern;

public class AuthenticationService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserDAO userDAO;
    private User currentUser;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public User register(String name, String email, String password, UserRole role) throws Exception {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name is required.");
        if (email == null || email.trim().isEmpty()) throw new IllegalArgumentException("Email/username is required.");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters.");

        if (role == null) role = UserRole.CUSTOMER;
        String normalizedIdentifier = email.trim();
        if (normalizedIdentifier.contains("@") && !EMAIL_PATTERN.matcher(normalizedIdentifier).matches()) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (!normalizedIdentifier.contains("@") && normalizedIdentifier.length() < 3) {
            throw new IllegalArgumentException("Username/email is too short.");
        }

        if (userDAO.findByEmail(normalizedIdentifier) != null || userDAO.findByUsername(normalizedIdentifier) != null) {
            throw new IllegalArgumentException("Duplicate account: " + normalizedIdentifier);
        }

        String hash = PasswordUtils.hashPassword(password);
        User user = new User(name.trim(), normalizedIdentifier, hash, role);
        userDAO.createUser(user);
        return user;
    }

    public User register(String username, String password, UserRole role) throws Exception {
        if (username == null || username.trim().isEmpty()) throw new IllegalArgumentException("Username/email is required.");
        String normalized = username.trim();
        return register(normalized, normalized, password, role);
    }

    public User register(String name, String email, String password) throws Exception {
        return register(name, email, password, UserRole.CUSTOMER);
    }

    public User register(String username, String password) throws Exception {
        return register(username, password, UserRole.CUSTOMER);
    }

    public User login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty()) return null;
        if (password == null) return null;

        String identifier = email.trim();
        User user = userDAO.findByEmail(identifier);
        if (user == null) user = userDAO.findByUsername(identifier);
        if (user == null) return null;
        if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) return null;
        this.currentUser = user;
        return user;
    }

    public void logout() { this.currentUser = null; }
    public boolean isAuthenticated() { return this.currentUser != null; }
    public User getCurrentUser() { return this.currentUser; }
    public boolean isAdmin() { return isAuthenticated() && currentUser.getRole() == UserRole.ADMIN; }
    public boolean requireAdmin() {
        if (!isAuthenticated() || !isAdmin()) throw new SecurityException("Admin access required.");
        return true;
    }
    public boolean requireCustomer() {
        if (!isAuthenticated() || currentUser.getRole() != UserRole.CUSTOMER) throw new SecurityException("Customer access required.");
        return true;
    }
}
