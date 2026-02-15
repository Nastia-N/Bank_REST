package com.nastian.bankcards.controller;

import com.nastian.bankcards.dto.CardRequest;
import com.nastian.bankcards.dto.CardResponse;
import com.nastian.bankcards.exception.ErrorResponse;
import com.nastian.bankcards.dto.PageCardResponse;
import com.nastian.bankcards.dto.PageUserResponse;
import com.nastian.bankcards.dto.UpdateRoleRequest;
import com.nastian.bankcards.dto.UserResponse;
import com.nastian.bankcards.entity.Card;
import com.nastian.bankcards.entity.CardStatus;
import com.nastian.bankcards.entity.User;
import com.nastian.bankcards.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для административных операций.
 * <p>
 * Предоставляет эндпоинты для:
 * <ul>
 *   <li>Управления пользователями (просмотр, изменение роли, удаление)</li>
 *   <li>Управления картами (просмотр, создание, изменение статуса, удаление)</li>
 *   <li>Поиска и фильтрации с пагинацией</li>
 * </ul>
 * Доступ только для пользователей с ролью ADMIN.
 */

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Controller", description = "Управление пользователями и картами (только для ADMIN)")
public class AdminController {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final AdminService adminService;

    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей с пагинацией и поиском. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = PageUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/users")
    public PageUserResponse getAllUsers(
            @Parameter(description = "Поиск по username или email (частичное совпадение)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Пагинация: page, size, sort. Пример: page=0&size=10&sort=createdAt,desc")
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<User> users = adminService.getAllUsers(search, pageable);
        return PageUserResponse.fromPage(users.map(this::toUserResponse));
    }

    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает детальную информацию о пользователе. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный ID пользователя",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/users/{userId}")
    public UserResponse getUserById(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long userId) {
        User user = adminService.getUserById(userId);
        return toUserResponse(user);
    }

    @Operation(summary = "Изменить роль пользователя",
            description = "Изменяет роль пользователя (USER/ADMIN). Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Роль успешно изменена",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверная роль (допустимо: USER, ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/users/{userId}/role")
    public UserResponse updateUserRole(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long userId,

            @Parameter(description = "Новая роль", required = true)
            @Valid @RequestBody UpdateRoleRequest request) {

        User user = adminService.updateUserRole(userId, request.getRole());
        return toUserResponse(user);
    }

    @Operation(summary = "Удалить пользователя",
            description = "Удаляет пользователя из системы. Все карты пользователя также будут удалены. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "400", description = "Неверный ID пользователя",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить все карты",
            description = "Возвращает список всех карт в системе с пагинацией и поиском. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен",
                    content = @Content(schema = @Schema(implementation = PageCardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/cards")
    public PageCardResponse getAllCards(
            @Parameter(description = "Поиск по маске номера карты (например: 1234)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Пагинация: page, size, sort")
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<Card> cards = adminService.getAllCards(search, pageable);
        return PageCardResponse.fromPage(cards.map(CardResponse::fromEntity));
    }

    @Operation(summary = "Получить карты пользователя",
            description = "Возвращает список карт указанного пользователя с пагинацией и поиском. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен",
                    content = @Content(schema = @Schema(implementation = PageCardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный ID пользователя или параметры запроса",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/users/{userId}/cards")
    public PageCardResponse getUserCards(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long userId,

            @Parameter(description = "Поиск по маске номера карты (например: 1234)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Пагинация: page, size, sort (например: sort=createdAt,desc)")
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<Card> cards = adminService.getUserCards(userId, search, pageable);
        return PageCardResponse.fromPage(cards.map(CardResponse::fromEntity));
    }

    @Operation(summary = "Создать карту для пользователя",
            description = "Создает новую карту для указанного пользователя. Номер генерируется автоматически. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно создана",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные карты",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/users/{userId}/cards")
    public CardResponse createCardForUser(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long userId,

            @Parameter(description = "Данные новой карты", required = true)
            @Valid @RequestBody CardRequest request) {

        Card card = adminService.createCardForUser(userId, request);
        return CardResponse.fromEntity(card);
    }

    @Operation(summary = "Изменить статус карты",
            description = "Блокирует или активирует карту. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус карты успешно изменен",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный статус (допустимо: ACTIVE, BLOCKED)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/cards/{cardId}/status")
    public CardResponse updateCardStatus(
            @Parameter(description = "ID карты", required = true, example = "1")
            @PathVariable Long cardId,

            @Parameter(description = "Новый статус", required = true, example = "BLOCKED")
            @RequestParam CardStatus status) {

        Card card = adminService.updateCardStatus(cardId, status);
        return CardResponse.fromEntity(card);
    }

    @Operation(summary = "Удалить карту",
            description = "Удаляет карту из системы. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "400", description = "Неверный ID карты",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "ID карты", required = true, example = "1")
            @PathVariable Long cardId) {
        adminService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}