package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    private UserRole role;
}
