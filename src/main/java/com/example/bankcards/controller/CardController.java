package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.security.CurrentUser;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class CardController {

    private final CardService cardService;
    private final TransferService transferService;

    public CardController(CardService cardService, TransferService transferService) {
        this.cardService = cardService;
        this.transferService = transferService;
    }

    @Operation(summary = "Получить свои карты",
            description = "Возвращает список карт текущего пользователя с пагинацией и поиском по номеру")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @GetMapping("/cards")
    public ResponseEntity<Page<CardResponse>> getMyCards(
            @Parameter(description = "Поиск по маске номера карты (например: 1234)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Пагинация и сортировка. Пример: page=0&size=10&sort=createdAt,desc")
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,

            @Parameter(hidden = true)
            @CurrentUser CustomUserDetails currentUser) {

        Page<Card> cards = cardService.getUserCards(currentUser.getId(), search, pageable);
        return ResponseEntity.ok(cards.map(CardResponse::fromEntity));
    }

    @Operation(summary = "Создать новую карту",
            description = "Создает новую карту для текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно создана",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные карты (имя или срок действия)"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PostMapping("/cards")
    public ResponseEntity<CardResponse> createCard(
            @Valid @RequestBody CardRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        Card card = cardService.createCard(request, currentUser.getId());
        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }

    @Operation(summary = "Заблокировать карту",
            description = "Переводит карту в статус BLOCKED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Карта уже заблокирована"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Карта принадлежит другому пользователю"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @PostMapping("/cards/{cardId}/block")
    public ResponseEntity<CardResponse> blockCard(
            @PathVariable Long cardId,
            @CurrentUser CustomUserDetails currentUser) {

        Card card = cardService.blockCard(cardId, currentUser.getId());
        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }

    @Operation(summary = "Активировать карту",
            description = "Переводит карту в статус ACTIVE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно активирована",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Карта уже активна или истек срок"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Карта принадлежит другому пользователю"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @PostMapping("/cards/{cardId}/activate")
    public ResponseEntity<CardResponse> activateCard(
            @PathVariable Long cardId,
            @CurrentUser CustomUserDetails currentUser) {

        Card card = cardService.activateCard(cardId, currentUser.getId());
        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }

    @Operation(summary = "Получить баланс карты",
            description = "Возвращает текущий баланс карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс успешно получен",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Карта принадлежит другому пользователю"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @GetMapping("/cards/{cardId}/balance")
    public ResponseEntity<Map<String, Object>> getCardBalance(
            @PathVariable Long cardId,
            @CurrentUser CustomUserDetails currentUser) {

        BigDecimal balance = cardService.getCardBalance(cardId, currentUser.getId());
        return ResponseEntity.ok(Map.of(
                "cardId", cardId,
                "balance", balance
        ));
    }

    @Operation(summary = "Перевод между своими картами",
            description = "Переводит средства с одной карты пользователя на другую")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Недостаточно средств, карта неактивна или перевод на ту же карту"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Одна из карт принадлежит другому пользователю"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @PostMapping("/transfers")
    public ResponseEntity<Map<String, Object>> transfer(
            @Valid @RequestBody TransferRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        Transfer transfer = transferService.transferBetweenOwnCards(request, currentUser.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Transfer completed successfully",
                "transferId", transfer.getId(),
                "amount", transfer.getAmount(),
                "fromCardId", transfer.getFromCard().getId(),
                "toCardId", transfer.getToCard().getId(),
                "timestamp", transfer.getTimestamp()
        ));
    }
}