package jdbc.domain.user.model;

import java.time.LocalDateTime;

public class User {
    private Long userId;
    private String email;
    private String name;
    private String password;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public User(Long userId, String email, String name, String password,
                UserRole role, LocalDateTime createdAt, LocalDateTime deletedAt) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("이메일은 필수입니다.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("이름은 필수입니다.");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("비밀번호는 필수입니다.");

        this.userId = userId;
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = (role == null) ? UserRole.USER : role;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}
