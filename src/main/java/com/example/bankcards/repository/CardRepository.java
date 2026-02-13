package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.cardNumberMasked LIKE %:search%")
    Page<Card> findByUserIdAndCardNumberMaskedContaining(@Param("userId") Long userId,
                                                         @Param("search") String search,
                                                         Pageable pageable);

    Page<Card> findAll(Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.cardNumberMasked LIKE %:search%")
    Page<Card> findAllByCardNumberMaskedContaining(@Param("search") String search, Pageable pageable);

    boolean existsById(Long id);
    void deleteById(Long id);
}