package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardResponse {

    private Long id;
    private String cardNumberMasked;
    private String cardHolderName;
    private LocalDate expirationDate;
    private String status;
    private BigDecimal balance;
    private Long userId;

    public static CardResponse fromEntity(Card card) {
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setCardNumberMasked(card.getCardNumberMasked());
        response.setCardHolderName(card.getCardHolderName());
        response.setExpirationDate(card.getExpirationDate());
        response.setStatus(card.getStatus());
        response.setBalance(card.getBalance());
        response.setUserId(card.getUser().getId());
        return response;
    }
}
