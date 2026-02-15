package com.nastian.bankcards.repository;

import com.nastian.bankcards.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью Transfer.
 */

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByFromCardId(Long cardId);

    List<Transfer> findByToCardId(Long cardId);

    @Query("SELECT t FROM Transfer t WHERE t.fromCard.user.id = :userId OR t.toCard.user.id = :userId")
    List<Transfer> findByUserId(@Param("userId") Long userId);
}