package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.UpdateRoleRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей с пагинацией и поиском. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN")
    })
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Поиск по username или email (частичное совпадение)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Пагинация: page, size, sort. Пример: page=0&size=10&sort=createdAt,desc")
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<User> users = adminService.getAllUsers(search, pageable);
        return ResponseEntity.ok(users.map(this::toUserResponse));
    }

    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает детальную информацию о пользователе. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long userId) {
        User user = adminService.getUserById(userId);
        return ResponseEntity.ok(toUserResponse(user));
    }

    @Operation(summary = "Изменить роль пользователя",
            description = "Изменяет роль пользователя (USER/ADMIN). Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Роль успешно изменена",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверная роль (допустимо: USER, ADMIN)"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long userId,

            @Parameter(description = "Новая роль", required = true)
            @Valid @RequestBody UpdateRoleRequest request) {

        User user = adminService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok(toUserResponse(user));
    }

    @Operation(summary = "Удалить пользователя",
            description = "Удаляет пользователя из системы. Все карты пользователя также будут удалены. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
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
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN")
    })
    @GetMapping("/cards")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @Parameter(description = "Поиск по маске номера карты (например: 1234)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Пагинация: page, size, sort")
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Card> cards = adminService.getAllCards(search, pageable);
        return ResponseEntity.ok(cards.map(CardResponse::fromEntity));
    }

    @Operation(summary = "Получить карты пользователя",
            description = "Возвращает список карт указанного пользователя с пагинацией и поиском. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Неверный ID пользователя или параметры запроса"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/users/{userId}/cards")
    public ResponseEntity<Page<CardResponse>> getUserCards(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long userId,

            @Parameter(description = "Поиск по маске номера карты (например: 1234)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Пагинация: page, size, sort (например: sort=createdAt,desc)")
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Card> cards = adminService.getUserCards(userId, search, pageable);
        return ResponseEntity.ok(cards.map(CardResponse::fromEntity));
    }

    @Operation(summary = "Создать карту для пользователя",
            description = "Создает новую карту для указанного пользователя. Номер генерируется автоматически. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно создана",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные карты"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<CardResponse> createCardForUser(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long userId,

            @Parameter(description = "Данные новой карты", required = true)
            @Valid @RequestBody CardRequest request) {

        Card card = adminService.createCardForUser(userId, request);
        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }

    @Operation(summary = "Изменить статус карты",
            description = "Блокирует или активирует карту. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус карты успешно изменен",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный статус (допустимо: ACTIVE, BLOCKED)"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @PutMapping("/cards/{cardId}/status")
    public ResponseEntity<CardResponse> updateCardStatus(
            @Parameter(description = "ID карты", required = true, example = "1")
            @PathVariable Long cardId,

            @Parameter(description = "Новый статус", required = true, example = "BLOCKED")
            @RequestParam String status) {

        Card card = adminService.updateCardStatus(cardId, status);
        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }

    @Operation(summary = "Удалить карту",
            description = "Удаляет карту из системы. Только для ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
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