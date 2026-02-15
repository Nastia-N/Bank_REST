package com.nastian.bankcards.controller;

import com.nastian.bankcards.dto.*;
import com.nastian.bankcards.entity.Card;
import com.nastian.bankcards.entity.Transfer;
import com.nastian.bankcards.exception.ErrorResponse;
import com.nastian.bankcards.security.CustomUserDetails;
import com.nastian.bankcards.security.CurrentUser;
import com.nastian.bankcards.service.CardService;
import com.nastian.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Контроллер для операций пользователя с его картами.
 * <p>
 * Предоставляет эндпоинты для:
 * <ul>
 *   <li>Просмотра своих карт с пагинацией и поиском</li>
 *   <li>Создания новой карты</li>
 *   <li>Управления статусом карты (блокировка/активация)</li>
 *   <li>Просмотра баланса</li>
 *   <li>Переводов между своими картами</li>
 * </ul>
 * Доступ только для аутентифицированных пользователей с ролью USER.
 */

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class CardController {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final CardService cardService;
    private final TransferService transferService;

    @Operation(summary = "Получить свои карты",
            description = "Возвращает список карт текущего пользователя с пагинацией и поиском по номеру")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен",
                    content = @Content(schema = @Schema(implementation = PageCardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/cards")
    public Page<CardResponse> getMyCards(
            @Parameter(description = "Поиск по маске номера карты (например: 1234)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Пагинация и сортировка. Пример: page=0&size=10&sort=createdAt,desc")
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,

            @Parameter(hidden = true)
            @CurrentUser CustomUserDetails currentUser) {

        Page<Card> cards = cardService.getUserCards(currentUser.getId(), search, pageable);
        return cards.map(CardResponse::fromEntity);
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
    public CardResponse createCard(
            @Valid @RequestBody CardRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        Card card = cardService.createCard(request, currentUser.getId());
        return CardResponse.fromEntity(card);
    }

    @Operation(summary = "Заблокировать карту",
            description = "Переводит карту в статус BLOCKED. Доступна только для своих карт")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Карта уже заблокирована"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Карта принадлежит другому пользователю"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @PostMapping("/cards/{cardId}/block")
    public CardResponse blockCard(
            @Parameter(description = "ID карты для блокировки", required = true, example = "1")
            @PathVariable Long cardId,
            @CurrentUser CustomUserDetails currentUser) {

        Card card = cardService.blockCard(cardId, currentUser.getId());
        return CardResponse.fromEntity(card);
    }

    @Operation(summary = "Активировать карту",
            description = "Переводит карту в статус ACTIVE. Доступна только для своих карт")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно активирована",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Карта уже активна или истек срок"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Карта принадлежит другому пользователю"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @PostMapping("/cards/{cardId}/activate")
    public CardResponse activateCard(
            @Parameter(description = "ID карты для активации", required = true, example = "1")
            @PathVariable Long cardId,
            @CurrentUser CustomUserDetails currentUser) {

        Card card = cardService.activateCard(cardId, currentUser.getId());
        return CardResponse.fromEntity(card);
    }

    @Operation(summary = "Получить баланс карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс успешно получен",
                    content = @Content(schema = @Schema(implementation = BalanceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Карта принадлежит другому пользователю",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/cards/{cardId}/balance")
    public BalanceResponse getCardBalance(@PathVariable Long cardId,
                                          @CurrentUser CustomUserDetails currentUser) {
        BigDecimal balance = cardService.getCardBalance(cardId, currentUser.getId());
        return new BalanceResponse(cardId, balance);
    }

    @Operation(summary = "Перевод между своими картами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен",
                    content = @Content(schema = @Schema(implementation = TransferResponse.class))),
            @ApiResponse(responseCode = "400", description = "Недостаточно средств, карта неактивна или перевод на ту же карту",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Одна из карт принадлежит другому пользователю",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/transfers")
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request,
                                     @CurrentUser CustomUserDetails currentUser) {
        Transfer transfer = transferService.transferBetweenOwnCards(request, currentUser.getId());

        return new TransferResponse(
                "Transfer completed successfully",
                transfer.getId(),
                transfer.getAmount(),
                transfer.getFromCard().getId(),
                transfer.getToCard().getId(),
                transfer.getTimestamp()
        );
    }
}