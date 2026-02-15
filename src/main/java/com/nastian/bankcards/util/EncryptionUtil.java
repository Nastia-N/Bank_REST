package com.nastian.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Утилита для шифрования и дешифрования данных.
 * <p>
 * Использует алгоритм AES для шифрования чувствительных данных,
 * таких как номера карт, перед сохранением в БД.
 */
@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private final SecretKeySpec key;

    /**
     * Создает утилиту с ключом шифрования из конфигурации.
     *
     * @param secretKey секретный ключ (должен быть 16, 24 или 32 байта)
     * @throws IllegalArgumentException если ключ не подходит по длине
     */
    public EncryptionUtil(@Value("${spring.encryption.secret-key}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes();
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException("Secret key must be 16, 24 or 32 bytes");
        }
        this.key = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Шифрует строку данных.
     *
     * @param data исходные данные (например, номер карты)
     * @return зашифрованная строка в Base64
     * @throws RuntimeException при ошибке шифрования
     */
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    /**
     * Дешифрует строку данных.
     *
     * @param encryptedData зашифрованные данные в Base64
     * @return исходные данные
     * @throws RuntimeException при ошибке дешифрования
     */
    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}