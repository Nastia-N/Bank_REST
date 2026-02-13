package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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
        transfer.setStatus("COMPLETED");

        return transferRepository.save(transfer);
    }
}