package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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

    // 👥 УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ

    public Page<User> getAllUsers(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return userRepository.searchUsers(search.trim(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public User updateUserRole(Long userId, UserRole newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }

    // 💳 УПРАВЛЕНИЕ КАРТАМИ

    public Page<Card> getAllCards(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return cardRepository.findAllByCardNumberMaskedContaining(search.trim(), pageable);
        }
        return cardRepository.findAll(pageable);
    }

    public Page<Card> getUserCards(Long userId, String search, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        if (search != null && !search.trim().isEmpty()) {
            return cardRepository.findByUserIdAndCardNumberMaskedContaining(userId, search.trim(), pageable);
        }
        return cardRepository.findByUserId(userId, pageable);
    }

    public Card createCardForUser(Long userId, CardRequest request) {
        return cardService.createCard(request, userId);
    }

    public Card updateCardStatus(Long cardId, String status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!"ACTIVE".equals(status) && !"BLOCKED".equals(status)) {
            throw new IllegalArgumentException("Invalid status. Allowed: ACTIVE, BLOCKED");
        }

        card.setStatus(status);
        return cardRepository.save(card);
    }

    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException(cardId);
        }
        cardRepository.deleteById(cardId);
    }
}
