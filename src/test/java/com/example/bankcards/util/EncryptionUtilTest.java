package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil("TestKey123456789");
    }

    @Test
    @DisplayName("Шифрование и дешифрование строки")
    void encryptDecrypt_Success() {
        String originalText = "1234567890123456";

        String encrypted = encryptionUtil.encrypt(originalText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertThat(encrypted).isNotEqualTo(originalText);
        assertThat(decrypted).isEqualTo(originalText);
    }

    @Test
    @DisplayName("Шифрование разных строк дает разные результаты")
    void encrypt_DifferentInputs_DifferentOutputs() {
        String text1 = "1234567890123456";
        String text2 = "1234567890123457";

        String encrypted1 = encryptionUtil.encrypt(text1);
        String encrypted2 = encryptionUtil.encrypt(text2);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("Дешифрование неверных данных выбрасывает исключение")
    void decrypt_InvalidData_ThrowsException() {
        String invalidData = "not-a-valid-base64";

        assertThatThrownBy(() -> encryptionUtil.decrypt(invalidData))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Decryption error");
    }

    @Test
    @DisplayName("Конструктор с ключом неверной длины выбрасывает исключение")
    void constructor_InvalidKeyLength_ThrowsException() {
        assertThatThrownBy(() -> new EncryptionUtil("short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Secret key must be 16, 24 or 32 bytes");
    }
}
