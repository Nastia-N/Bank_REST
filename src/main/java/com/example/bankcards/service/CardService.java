package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.CardNumberMasker;
import com.example.bankcards.util.EncryptionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
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
        card.setStatus("ACTIVE");
        card.setBalance(BigDecimal.ZERO);

        return cardRepository.save(card);
    }

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

    @Transactional(readOnly = true)
    public Card getCardAndValidateOwnership(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!card.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException(userId, "card " + cardId);
        }

        return card;
    }

    public Card blockCard(Long cardId, Long userId) {
        Card card = getCardAndValidateOwnership(cardId, userId);

        if ("BLOCKED".equals(card.getStatus())) {
            throw new CardNotActiveException("Card is already blocked");
        }

        card.setStatus("BLOCKED");
        return cardRepository.save(card);
    }

    public Card activateCard(Long cardId, Long userId) {
        Card card = getCardAndValidateOwnership(cardId, userId);

        if ("ACTIVE".equals(card.getStatus())) {
            throw new CardNotActiveException("Card is already active");
        }

        if (card.getExpirationDate().isBefore(LocalDate.now())) {
            throw new CardNotActiveException("Cannot activate expired card");
        }

        card.setStatus("ACTIVE");
        return cardRepository.save(card);
    }

    public Card addFunds(Long cardId, Long userId, BigDecimal amount) {
        Card card = getCardAndValidateOwnership(cardId, userId);
        card.setBalance(card.getBalance().add(amount));
        return cardRepository.save(card);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCardBalance(Long cardId, Long userId) {
        Card card = getCardAndValidateOwnership(cardId, userId);
        return card.getBalance();
    }

    @Transactional(readOnly = true)
    public void validateCardActive(Card card) {
        if (!"ACTIVE".equals(card.getStatus())) {
            throw new CardNotActiveException(card.getId(), card.getStatus());
        }
        if (card.getExpirationDate().isBefore(LocalDate.now())) {
            throw new CardNotActiveException("Card " + card.getId() + " has expired");
        }
    }
}