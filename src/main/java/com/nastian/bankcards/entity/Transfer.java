package com.nastian.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность перевода средств между картами.
 * <p>
 * Хранит информацию о переводе:
 * <ul>
 *   <li>Карта-отправитель</li>
 *   <li>Карта-получатель</li>
 *   <li>Сумма перевода</li>
 *   <li>Дата и время выполнения</li>
 *   <li>Статус перевода</li>
 * </ul>
 */

@Entity
@Table(name = "transfers", indexes = {
        @Index(name = "idx_transfer_from_card", columnList = "from_card_id"),
        @Index(name = "idx_transfer_to_card", columnList = "to_card_id"),
        @Index(name = "idx_transfer_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id", nullable = false)
    private Card toCard;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferStatus status = TransferStatus.COMPLETED;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}