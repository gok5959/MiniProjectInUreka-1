package jdbc.domain.user.dto;

import jdbc.domain.user.model.User;
import jdbc.domain.user.model.UserRole;

import java.time.format.DateTimeFormatter;

public class UserDto {
    private Long userId;
    private String email;
    private String name;
    private UserRole role;
    private String createdAt;
    private String deletedAt;

    public UserDto(Long userId, String email, String name, UserRole role, String createdAt, String deletedAt) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public static UserDto from(User user) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return new UserDto(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getCreatedAt() != null ? user.getCreatedAt().format(fmt) : null,
                user.getDeletedAt() != null ? user.getDeletedAt().format(fmt) : null);
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDeletedAt() {
        return deletedAt;
    }
}
