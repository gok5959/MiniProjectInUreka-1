//package jdbc.domain.user.model;
//
//import java.time.LocalDateTime;
//
//public class User {
//    private Long userId;
//    private String email;
//    private String name;
//    private String password;
//    private UserRole role;
//    private LocalDateTime createdAt;
//    private LocalDateTime deletedAt;
//
//    public User(Long userId, String email, String name, String password,
//                UserRole role, LocalDateTime createdAt, LocalDateTime deletedAt) {
//        if (email == null || email.isBlank()) throw new IllegalArgumentException("이메일은 필수입니다.");
//        if (name == null || name.isBlank()) throw new IllegalArgumentException("이름은 필수입니다.");
//        if (password == null || password.isBlank()) throw new IllegalArgumentException("비밀번호는 필수입니다.");
//
//        this.userId = userId;
//        this.email = email;
//        this.name = name;
//        this.password = password;
//        this.role = (role == null) ? UserRole.USER : role;
//        this.createdAt = createdAt;
//        this.deletedAt = deletedAt;
//    }
//
//    public Long getUserId() { return userId; }
//    public String getEmail() { return email; }
//    public String getName() { return name; }
//    public String getPassword() { return password; }
//    public UserRole getRole() { return role; }
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public LocalDateTime getDeletedAt() { return deletedAt; }
//
//    public boolean isDeleted() {
//        return deletedAt != null;
//    }
//
//    public void markDeleted() {
//        this.deletedAt = LocalDateTime.now();
//    }
//}
package jdbc.domain.user.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(name = "unq_users_email", columnNames = "email"))
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role = UserRole.USER;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamp")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // soft delete

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public User(Long userId, String email, String name, String password, UserRole role, LocalDateTime createdAt, LocalDateTime deletedAt) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    // 기본 생성자 & getter/setter
    protected User() {
    }

    public User(Long userId, String email, String name, String password, UserRole role, LocalDateTime createdAt) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
    }

    public User(String email, String name, String password, UserRole role) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role == null ? UserRole.USER : role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // getters / setters ...
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
