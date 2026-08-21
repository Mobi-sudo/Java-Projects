package service;

import dao.UserDAO;
import model.User;

public class AuthenticationService {
    private final UserDAO userDAO = new UserDAO();

    public boolean registerUser(String username, char[] password, String role, Integer studentId) {
        if (username == null || username.trim().isEmpty()) return false;
        if (password == null || password.length == 0) return false;
        String up = username.trim();
        String r = role == null ? "STUDENT" : role.trim().toUpperCase();
        if (!r.equals("ADMIN") && !r.equals("STUDENT")) return false;
        if (userDAO.usernameExists(up)) return false;
        byte[] salt = AuthUtil.generateSalt();
        byte[] hash = AuthUtil.hashPassword(password, salt);
        User u = new User(up, hash, salt, r, studentId);
        return userDAO.createUser(u);
    }

    public User authenticate(String username, char[] password) {
        if (username == null || password == null) return null;
        User u = userDAO.findByUsername(username);
        if (u == null) return null;
        boolean ok = AuthUtil.verifyPassword(password, u.getSalt(), u.getPasswordHash());
        return ok ? u : null;
    }
}
