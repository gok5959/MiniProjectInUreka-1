package jdbc.domain.user.model;

public enum UserRole {
    ADMIN, USER;

    public static UserRole fromString(String role) {
        if(role == null) return USER;
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("유저 Role이 정의되지 않았습니다. : " + role);
        }
    }
}
