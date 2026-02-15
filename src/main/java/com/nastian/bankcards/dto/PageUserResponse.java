package com.nastian.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO для страницы с пользователями.
 * <p>
 * Используется для документирования пагинированного ответа с пользователями.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Страница с пользователями")
public class PageUserResponse {

    @Schema(description = "Список пользователей на текущей странице")
    private List<UserResponse> content;

    @Schema(description = "Номер текущей страницы (начиная с 0)", example = "0")
    private int number;

    @Schema(description = "Размер страницы", example = "10")
    private int size;

    @Schema(description = "Общее количество элементов", example = "25")
    private long totalElements;

    @Schema(description = "Общее количество страниц", example = "3")
    private int totalPages;

    @Schema(description = "Есть ли следующая страница", example = "true")
    private boolean hasNext;

    @Schema(description = "Есть ли предыдущая страница", example = "false")
    private boolean hasPrevious;

    @Schema(description = "Первая ли это страница", example = "true")
    private boolean first;

    @Schema(description = "Последняя ли это страница", example = "false")
    private boolean last;

    public static PageUserResponse fromPage(Page<UserResponse> page) {
        return new PageUserResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious(),
                page.isFirst(),
                page.isLast()
        );
    }
}
