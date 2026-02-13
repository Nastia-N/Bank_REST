package com.example.bankcards.dto;

import lombok.Data;

@Data
public class UpdateCardStatusRequest {
    private String status; // ACTIVE, BLOCKED
}
