package jdbc.domain.order.model;

public enum ReviewStatus {
    BEFORE_REVIEW, REVIEWED;

    public static ReviewStatus fromDb(String s) {
        return s == null ? null : ReviewStatus.valueOf(s.toUpperCase());
    }

    public String toDb() {
        return name();
    }
}
