package model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private UserRole role;
    private AccountStatus status;
    private LocalDateTime createdAt;

    public User() {
        this.createdAt = LocalDateTime.now();
        this.status = AccountStatus.ACTIVE;
    }

    public User(int id, String name, String email, String passwordHash, UserRole role, LocalDateTime createdAt) {
        this(id, name, email, passwordHash, role, AccountStatus.ACTIVE, createdAt);
    }

    public User(int id, String name, String email, String passwordHash, UserRole role,
                AccountStatus status, LocalDateTime createdAt) {
        setId(id);
        setName(name);
        setEmail(email);
        setPasswordHash(passwordHash);
        setRole(role);
        setStatus(status);
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public User(String name, String email, String passwordHash, UserRole role) {
        this(0, name, email, passwordHash, role, LocalDateTime.now());
    }

    public User(String name, String email, UserRole role) {
        this(name, email, "", role);
    }

    public int getId() { return id; }
    public void setId(int id) {
        if (id < 0) throw new IllegalArgumentException("ID cannot be negative.");
        this.id = id;
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name is required.");
        this.name = name.trim();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) throw new IllegalArgumentException("Email is required.");
        this.email = email.trim();
    }

    public String getUsername() { return email; }
    public void setUsername(String username) { setEmail(username); }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) throw new IllegalArgumentException("Password hash is required.");
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) {
        if (role == null) throw new IllegalArgumentException("Role is required.");
        this.role = role;
    }

    public AccountStatus getStatus() { return status == null ? AccountStatus.ACTIVE : status; }
    public void setStatus(AccountStatus status) {
        if (status == null) throw new IllegalArgumentException("Status is required.");
        this.status = status;
    }

    public boolean isActive() { return getStatus() == AccountStatus.ACTIVE; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() { return this.role == UserRole.ADMIN; }
    public boolean isCustomer() { return this.role == UserRole.CUSTOMER; }
}
