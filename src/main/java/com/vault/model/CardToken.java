package com.vault.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "card_tokens", indexes = {
    @Index(name = "idx_token", columnList = "token", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String token;

    @Column(nullable = false, length = 512)
    private String encryptedPan;

    @Column(nullable = false, length = 64)
    private String iv;

    @Column(nullable = false, length = 4)
    private String lastFour;
}
