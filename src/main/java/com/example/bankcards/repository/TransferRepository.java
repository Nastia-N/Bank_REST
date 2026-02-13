package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    // Найти все переводы с карты
    List<Transfer> findByFromCardId(Long cardId);

    // Найти все переводы на карту
    List<Transfer> findByToCardId(Long cardId);

    // Найти все переводы пользователя (по его картам)
    @Query("SELECT t FROM Transfer t WHERE t.fromCard.user.id = :userId OR t.toCard.user.id = :userId")
    List<Transfer> findByUserId(@Param("userId") Long userId);
}
