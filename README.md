# 🔒 Token Vault — PCI-DSS Credit Card Tokenization Microservice

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Security](https://img.shields.io/badge/Cryptography-AES--256--GCM-blue.svg)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/crypto/Cipher.html)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A high-performance, zero-trust **Payment Card Industry Data Security Standard (PCI-DSS)** tokenization engine engineered with **Java 21** and **Spring Boot 3**. 

This microservice acts as an isolated security vault that converts sensitive Primary Account Numbers (PANs) into non-sensitive surrogate tokens (`TKN-XXXXXX`). By preventing raw card details from being stored in primary database storage, it drastically reduces system audit overhead and compliance liability under PCI-DSS Requirement 3.

---

## 📌 Executive Summary & Architecture

In modern fintech and payment gateway architectures, handling raw 16-digit credit card numbers introduces high regulatory risk. The **Token Vault** service solves this by isolating the cryptographic boundary:

1. **Card Validation:** Incoming payloads are validated against **Luhn's Algorithm** (ISO/IEC 7812-1) before any processing.
2. **Authenticated Encryption:** Raw card numbers are encrypted using **AES-256-GCM** with dynamically generated 12-byte Initialization Vectors (IVs) via `SecureRandom`.
3. **Token Mapping:** Real card details are replaced by isolated surrogate tokens (`TKN-XXXXXX`), storing only the Base64 cryptogram, IV, and last 4 digits in an isolated database.
4. **Volatile Payment Processing:** Decryption occurs strictly in-memory during mock payment authorization workflows and is immediately discarded from memory.

---

## 🛠️ Tech Stack & Key Specifications

* **Language & Runtime:** Java 21 (JDK 21 LTS)
* **Framework:** Spring Boot 3.3.x, Spring Web (REST API), Spring Data JPA
* **Security & Cryptography:** Java Cryptography Extension (`javax.crypto`), AES-256-GCM, `SecureRandom`, Luhn's Algorithm
* **Database & Persistence:** H2 In-Memory Database (Spring Data JPA / Hibernate)
* **Testing:** JUnit 5, Spring Boot Test (`MockMvc`)
* **Build & Version Control:** Apache Maven, Git

---

## 🚀 Key Features

* 🔐 **AES-256-GCM AEAD Security:** Utilizes Authenticated Encryption with Associated Data (AEAD) to prevent ciphertext tampering and replay attacks.
* 🛡️ **PCI-DSS Scope Reduction:** Keeps application databases, logs, and external responses free from plaintext credit card numbers (PANs).
* ⚡ **Zero-Persistence Decryption:** Card numbers exist in plaintext only during transient processing and are never written to disk or logs.
* 💳 **Luhn Algorithm Verification:** Rejects invalid, spoofed, or mistyped card numbers prior to executing expensive cryptographic operations.
* 📊 **Compliance Database Inspector:** Built-in web dashboard allowing security auditors to verify that only Base64 cryptograms and tokens are persisted.
* 🧪 **Automated Testing Suite:** End-to-end API integration testing using JUnit 5 and Spring `MockMvc`.

---

## 🔗 Acknowledgments & References

This project draws architectural inspiration from open-source secure storage models, specifically referencing:
* **Reference Vault Architecture:** [java-password-vault](https://github.com/stormtheory/java-password-vault) — *Conceptual reference for structural data vaulting, key isolation patterns, and secure record mapping.*
* **Compliance Standards:** [PCI-DSS v4.0 Requirement 3](https://www.pcisecuritystandards.org/) — *Guidelines for Protecting Stored Account Data.*

---

## ⚙️ Getting Started

### Prerequisites

* **Java Development Kit (JDK 21)** or higher
* **Apache Maven 3.8+**
* **Git**

### Installation & Local Run

1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/AyushiVadariya/token-vault-pci-service.git](https://github.com/AyushiVadariya/token-vault-pci-service.git)
   cd token-vault-pci-service
