package jdbc.common.paging;

public record PageRequest(int limit, int offset) {
    public static PageRequest of(int limit, int offset) {
        if (limit <= 0) throw new IllegalArgumentException("limit must be greater than 0");
        if (offset <= 0) throw new IllegalArgumentException("offset must be greater than 0");
        return new PageRequest(limit, offset);
    }
}
