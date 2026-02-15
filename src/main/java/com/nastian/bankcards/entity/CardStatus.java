package com.nastian.bankcards.entity;

/**
 * Статус банковской карты.
 * <p>
 * Возможные состояния:
 * <ul>
 *   <li>ACTIVE - карта активна, доступны операции</li>
 *   <li>BLOCKED - карта заблокирована (пользователем или администратором)</li>
 *   <li>EXPIRED - срок действия карты истек</li>
 * </ul>
 */

public enum CardStatus {
    ACTIVE,
    BLOCKED,
    EXPIRED
}