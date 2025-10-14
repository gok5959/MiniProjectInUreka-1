package jdbc.domain.order.model;

public enum OrderState {
    REQUESTED, ACCEPTED, REJECTED, CANCELLED_BY_BUYER, CANCELLED_BY_SELLER, COMPLETED;

    public static OrderState fromDb(String s) {
        return s == null ? null : OrderState.valueOf(s);
    }

    public String toDb() {
        return name();
    }
}
