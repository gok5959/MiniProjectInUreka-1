package jdbc.domain.category.dto;

public record CategoryDto(
        Long categoryId, String name, Long parentId
) {
}
