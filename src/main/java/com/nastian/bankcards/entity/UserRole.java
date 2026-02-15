package com.nastian.bankcards.entity;

/**
 * Роли пользователей в системе.
        * <p>
 * <ul>
 *   <li>USER - обычный пользователь, работает только со своими картами</li>
 *   <li>ADMIN - администратор, полный доступ ко всем операциям</li>
 * </ul>
 */

public enum UserRole {
    USER,
    ADMIN
}
