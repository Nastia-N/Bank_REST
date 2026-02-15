package com.nastian.bankcards.service;

import com.nastian.bankcards.dto.CardRequest;
import com.nastian.bankcards.entity.Card;
import com.nastian.bankcards.entity.CardStatus;
import com.nastian.bankcards.entity.User;
import com.nastian.bankcards.entity.UserRole;
import com.nastian.bankcards.exception.CardNotFoundException;
import com.nastian.bankcards.exception.UserNotFoundException;
import com.nastian.bankcards.repository.CardRepository;
import com.nastian.bankcards.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для административных операций.
 * <p>
 * Предоставляет функциональность для:
 * <ul>
 *   <li>Управления пользователями (просмотр, изменение роли, удаление)</li>
 *   <li>Управления картами (просмотр, создание, изменение статуса, удаление)</li>
 * </ul>
 * Все методы доступны только для пользователей с ролью ADMIN.
 */
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;

    public AdminService(UserRepository userRepository,
                        CardRepository cardRepository,
                        CardService cardService) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.cardService = cardService;
    }

    /**
     * Получение всех пользователей с поиском и пагинацией.
     *
     * @param search поисковый запрос (по username или email)
     * @param pageable параметры пагинации
     * @return страница с пользователями
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return userRepository.searchUsers(search.trim(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    /**
     * Получение пользователя по ID.
     *
     * @param userId ID пользователя
     * @return пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Изменение роли пользователя.
     *
     * @param userId ID пользователя
     * @param newRole новая роль (USER или ADMIN)
     * @return обновленный пользователь
     */
    @Transactional
    public User updateUserRole(Long userId, UserRole newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        return userRepository.save(user);
    }

    /**
     * Удаление пользователя.
     *
     * @param userId ID пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }

    /**
     * Получение всех карт с поиском по номеру и пагинацией.
     *
     * @param search маска номера карты
     * @param pageable параметры пагинации
     * @return страница с картами
     */
    @Transactional(readOnly = true)
    public Page<Card> getAllCards(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return cardRepository.findAllByCardNumberMaskedContaining(search.trim(), pageable);
        }
        return cardRepository.findAll(pageable);
    }

    /**
     * Получение карт конкретного пользователя с поиском.
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
     * Создание карты для пользователя.
     *
     * @param userId ID пользователя
     * @param request данные карты
     * @return созданная карта
     */
    @Transactional
    public Card createCardForUser(Long userId, CardRequest request) {
        return cardService.createCard(request, userId);
    }

    /**
     * Изменение статуса карты.
     *
     * @param cardId ID карты
     * @param status новый статус (ACTIVE или BLOCKED)
     * @return обновленная карта
     * @throws CardNotFoundException если карта не найдена
     */
    @Transactional
    public Card updateCardStatus(Long cardId, CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (status != CardStatus.ACTIVE && status != CardStatus.BLOCKED) {
            throw new IllegalArgumentException("Status can only be changed to ACTIVE or BLOCKED");
        }

        card.setStatus(status);
        return cardRepository.save(card);
    }

    /**
     * Удаление карты.
     *
     * @param cardId ID карты
     * @throws CardNotFoundException если карта не найдена
     */
    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException(cardId);
        }
        cardRepository.deleteById(cardId);
    }
}