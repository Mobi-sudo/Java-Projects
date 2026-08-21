package model;

public class User {
    private int userId;
    private String username;
    private byte[] passwordHash;
    private byte[] salt;
    private String role; // "ADMIN" or "STUDENT"
    private Integer studentId; // nullable

    public User(int userId, String username, byte[] passwordHash, byte[] salt, String role, Integer studentId) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.studentId = studentId;
    }

    public User(String username, byte[] passwordHash, byte[] salt, String role, Integer studentId) {
        this(0, username, passwordHash, salt, role, studentId);
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public byte[] getPasswordHash() { return passwordHash; }
    public byte[] getSalt() { return salt; }
    public String getRole() { return role; }
    public Integer getStudentId() { return studentId; }
}
