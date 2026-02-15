package com.nastian.bankcards.service;

import com.nastian.bankcards.dto.TransferRequest;
import com.nastian.bankcards.entity.Card;
import com.nastian.bankcards.entity.Transfer;
import com.nastian.bankcards.entity.TransferStatus;
import com.nastian.bankcards.exception.CardNotActiveException;
import com.nastian.bankcards.exception.InsufficientFundsException;
import com.nastian.bankcards.repository.CardRepository;
import com.nastian.bankcards.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для переводов между картами.
 * <p>
 * Предоставляет функциональность для:
 * <ul>
 *   <li>Переводов между своими картами</li>
 *   <li>Проверки баланса</li>
 *   <li>Сохранения истории переводов</li>
 * </ul>
 */

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;

    public TransferService(TransferRepository transferRepository,
                           CardRepository cardRepository,
                           CardService cardService) {
        this.transferRepository = transferRepository;
        this.cardRepository = cardRepository;
        this.cardService = cardService;
    }

    /**
     * Перевод средств между своими картами.
     *
     * @param request данные перевода (fromCardId, toCardId, amount)
     * @param userId ID владельца карт
     * @return совершенный перевод
     * @throws IllegalArgumentException если карты совпадают
     * @throws InsufficientFundsException если недостаточно средств
     * @throws CardNotActiveException если карта неактивна
     */
    @Transactional
    public Transfer transferBetweenOwnCards(TransferRequest request, Long userId) {

        Card fromCard = cardService.getCardAndValidateOwnership(request.getFromCardId(), userId);
        Card toCard = cardService.getCardAndValidateOwnership(request.getToCardId(), userId);

        if (fromCard.getId().equals(toCard.getId())) {
            throw new IllegalArgumentException("Cannot transfer money to the same card");
        }

        cardService.validateCardActive(fromCard);
        cardService.validateCardActive(toCard);

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(
                    fromCard.getId(),
                    fromCard.getBalance(),
                    request.getAmount()
            );
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transfer transfer = new Transfer();
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setAmount(request.getAmount());
        transfer.setStatus(TransferStatus.COMPLETED);

        return transferRepository.save(transfer);
    }
}