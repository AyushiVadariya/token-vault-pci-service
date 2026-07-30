package com.vault.controller;

import com.vault.crypto.CryptoService;
import com.vault.model.CardToken;
import com.vault.repository.CardTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vault")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class VaultController {

    private final CardTokenRepository cardTokenRepository;
    private final CryptoService cryptoService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * POST /api/v1/vault/tokenize
     * Accepts: { "cardNumber": "..." }
     * Generates a surrogate token TKN-XXXXXX, encrypts card using AES-GCM, saves to H2 DB,
     * returns { "token": "...", "lastFour": "..." }.
     */
    @PostMapping("/tokenize")
    public ResponseEntity<?> tokenize(@RequestBody TokenizeRequest request) {
        String cardNumber = request.getCardNumber();
        if (cardNumber == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Card number is required"));
        }
        
        // Clean card number from whitespace/dashes
        cardNumber = cardNumber.replaceAll("\\s|-", "");

        // Valid length is 13 to 19 digits and must pass Luhn check
        if (!cardNumber.matches("\\d{13,19}") || !luhnCheck(cardNumber)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid card number format or fails Luhn algorithm check"));
        }

        try {
            String token = generateUniqueToken();
            String lastFour = cardNumber.substring(cardNumber.length() - 4);

            // Encrypt using AES-256-GCM
            CryptoService.EncryptionResult encResult = cryptoService.encrypt(cardNumber);

            // Save to DB
            CardToken cardToken = CardToken.builder()
                    .token(token)
                    .encryptedPan(encResult.getEncryptedData())
                    .iv(encResult.getIv())
                    .lastFour(lastFour)
                    .build();

            cardTokenRepository.save(cardToken);

            return ResponseEntity.ok(new TokenizeResponse(token, lastFour));
        } catch (Exception e) {
            log.error("Tokenization failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Tokenization failed due to an internal cryptographic or system error"));
        }
    }

    /**
     * POST /api/v1/vault/pay
     * Accepts: { "token": "...", "amount": 50 }
     * Fetches encrypted PAN by token, decrypts it in-memory, verifies format,
     * and returns { "status": "APPROVED", "transactionId": "..." }.
     */
    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody PayRequest request) {
        String token = request.getToken();
        BigDecimal amount = request.getAmount();

        if (token == null || amount == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token and amount are required"));
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Amount must be greater than zero"));
        }

        Optional<CardToken> optionalCardToken = cardTokenRepository.findByToken(token);
        if (optionalCardToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Surrogate token not found in the vault"));
        }

        CardToken cardToken = optionalCardToken.get();

        try {
            // Decrypt PAN in-memory
            String decryptedPan = cryptoService.decrypt(cardToken.getEncryptedPan(), cardToken.getIv());

            // Verify decrypted format (13-19 digits)
            if (!decryptedPan.matches("\\d{13,19}")) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(Map.of("error", "Decrypted PAN has invalid format"));
            }

            // Generate mock transaction ID
            String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 18).toUpperCase();

            return ResponseEntity.ok(new PayResponse("APPROVED", transactionId));
        } catch (Exception e) {
            log.error("Payment processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment authorization failed during decrypt/processing"));
        }
    }

    /**
     * GET /api/v1/vault/tokens
     * Debugging utility to view the encrypted token table entries in the database.
     */
    @GetMapping("/tokens")
    public ResponseEntity<?> getTokens() {
        return ResponseEntity.ok(cardTokenRepository.findAll());
    }

    private String generateUniqueToken() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        while (true) {
            StringBuilder sb = new StringBuilder("TKN-");
            for (int i = 0; i < 6; i++) {
                sb.append(characters.charAt(secureRandom.nextInt(characters.length())));
            }
            String candidate = sb.toString();
            if (cardTokenRepository.findByToken(candidate).isEmpty()) {
                return candidate;
            }
        }
    }

    private boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenizeRequest {
        private String cardNumber;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class TokenizeResponse {
        private String token;
        private String lastFour;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PayRequest {
        private String token;
        private BigDecimal amount;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PayResponse {
        private String status;
        private String transactionId;
    }
}
