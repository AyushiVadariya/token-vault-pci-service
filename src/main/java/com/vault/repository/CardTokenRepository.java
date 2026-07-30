package com.vault.repository;

import com.vault.model.CardToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CardTokenRepository extends JpaRepository<CardToken, Long> {
    Optional<CardToken> findByToken(String token);
}
