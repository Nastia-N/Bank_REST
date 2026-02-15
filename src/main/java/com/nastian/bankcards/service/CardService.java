package com.nastian.bankcards.service;

import com.nastian.bankcards.dto.CardRequest;
import com.nastian.bankcards.entity.Card;
import com.nastian.bankcards.entity.CardStatus;
import com.nastian.bankcards.entity.User;
import com.nastian.bankcards.exception.CardNotActiveException;
import com.nastian.bankcards.exception.CardNotFoundException;
import com.nastian.bankcards.exception.UnauthorizedAccessException;
import com.nastian.bankcards.exception.UserNotFoundException;
import com.nastian.bankcards.repository.CardRepository;
import com.nastian.bankcards.repository.UserRepository;
import com.nastian.bankcards.util.CardNumberGenerator;
import com.nastian.bankcards.util.CardNumberMasker;
import com.nastian.bankcards.util.EncryptionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Сервис для операций с картами.
 * <p>
 * Предоставляет функциональность для:
 * <ul>
 *   <li>Создания карт</li>
 *   <li>Просмотра карт пользователя</li>
 *   <li>Блокировки/активации карт</li>
 *   <li>Проверки баланса</li>
 *   <li>Валидации владения и статуса карт</li>
 * </ul>
 */
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final CardNumberGenerator cardNumberGenerator;
    private final CardNumberMasker cardNumberMasker;

    public CardService(CardRepository cardRepository,
                       UserRepository userRepository,
                       EncryptionUtil encryptionUtil,
                       CardNumberGenerator cardNumberGenerator,
                       CardNumberMasker cardNumberMasker) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
        this.cardNumberGenerator = cardNumberGenerator;
        this.cardNumberMasker = cardNumberMasker;
    }

    /**
     * Создание новой карты для пользователя.
     *
     * @param request данные карты (имя владельца, срок действия)
     * @param userId ID владельца
     * @return созданная карта
     * @throws UserNotFoundException если пользователь не найден
     */
    @Transactional
    public Card createCard(CardRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String cardNumber = cardNumberGenerator.generate();
        String encrypted = encryptionUtil.encrypt(cardNumber);
        String masked = cardNumberMasker.mask(cardNumber);

        Card card = new Card();
        card.setCardHolderName(request.getCardHolderName());
        card.setExpirationDate(request.getExpirationDate());
        card.setCardNumberEncrypted(encrypted);
        card.setCardNumberMasked(masked);
        card.setUser(user);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        return cardRepository.save(card);
    }

    /**
     * Получение карт пользователя с поиском и пагинацией.
     *
     * @param userId ID пользователя
     * @param search маска номера карты
     * @param pageable параметры пагинации
     * @return страница с картами
     * @throws UserNotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public Page<Card> getUserCards(Long userId, String search, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        if (search != null && !search.trim().isEmpty()) {
            return cardRepository.findByUserIdAndCardNumberMaskedContaining(userId, search.trim(), pageable);
        }
        return cardRepository.findByUserId(userId, pageable);
    }

    /**
     * Получение карты с проверкой принадлежности пользователю.
     *
     * @param cardId ID карты
     * @param userId ID пользователя
     * @return карта
     * @throws CardNotFoundException если карта не найдена
     * @throws UnauthorizedAccessException если карта принадлежит другому пользователю
     */
    @Transactional(readOnly = true)
    public Card getCardAndValidateOwnership(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException(userId, "card " + cardId);
        }

        return card;
    }

    /**
     * Блокировка карты.
     *
     * @param cardId ID карты
     * @param userId ID владельца
     * @return обновленная карта
     * @throws CardNotActiveException если карта уже заблокирована
     */
    @Transactional
    public Card blockCard(Long cardId, Long userId) {
        Card card = getCardAndValidateOwnership(cardId, userId);

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardNotActiveException("Card is already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    /**
     * Активация карты.
     *
     * @param cardId ID карты
     * @param userId ID владельца
     * @return обновленная карта
     * @throws CardNotActiveException если карта уже активна или истек срок
     */
    @Transactional
    public Card activateCard(Long cardId, Long userId) {
        Card card = getCardAndValidateOwnership(cardId, userId);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardNotActiveException("Card is already active");
        }

        if (card.getExpirationDate().isBefore(LocalDate.now())) {
            throw new CardNotActiveException("Cannot activate expired card");
        }

        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    /**
     * Получение баланса карты.
     *
     * @param cardId ID карты
     * @param userId ID владельца
     * @return баланс
     */
    @Transactional(readOnly = true)
    public BigDecimal getCardBalance(Long cardId, Long userId) {
        Card card = getCardAndValidateOwnership(cardId, userId);
        return card.getBalance();
    }

    /**
     * Валидация активности карты.
     *
     * @param card карта для проверки
     * @throws CardNotActiveException если карта заблокирована или истек срок
     */
    public void validateCardActive(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException(card.getId(), card.getStatus().name());
        }
        if (card.getExpirationDate().isBefore(LocalDate.now())) {
            throw new CardNotActiveException("Card " + card.getId() + " has expired");
        }
    }
}