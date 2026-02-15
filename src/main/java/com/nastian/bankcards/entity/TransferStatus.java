package com.nastian.bankcards.entity;

/**
 * Статус перевода средств.
 * <p>
 * <ul>
 *   <li>COMPLETED - перевод успешно выполнен</li>
 *   <li>FAILED - перевод не удался</li>
 *   <li>CANCELLED - перевод отменен</li>
 * </ul>
 */

public enum TransferStatus {
    COMPLETED,
    FAILED,
    CANCELLED
}