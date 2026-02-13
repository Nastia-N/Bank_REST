package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardNumberGeneratorTest {

    private CardNumberGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CardNumberGenerator();
    }

    @Test
    @DisplayName("Генерация 16-значного номера")
    void generate_Returns16DigitNumber() {
        String number = generator.generate();

        assertThat(number).hasSize(16);
        assertThat(number).containsOnlyDigits();
    }

    @Test
    @DisplayName("Генерация с префиксом '4' (как Visa)")
    void generateWithPrefix_VisaPrefix() {
        String number = generator.generateWithPrefix("4");

        assertThat(number).hasSize(16);
        assertThat(number).startsWith("4");
        assertThat(number).containsOnlyDigits();
    }

    @Test
    @DisplayName("Генерация с пустым префиксом")
    void generateWithPrefix_EmptyPrefix() {
        String number = generator.generateWithPrefix("");

        assertThat(number).hasSize(16);
        assertThat(number).containsOnlyDigits();
    }

    @Test
    @DisplayName("Генерация с null префиксом")
    void generateWithPrefix_NullPrefix() {
        String number = generator.generateWithPrefix(null);

        assertThat(number).hasSize(16);
        assertThat(number).containsOnlyDigits();
    }

    @Test
    @DisplayName("Генерация с коротким префиксом")
    void generateWithPrefix_ShortPrefix() {
        String prefix = "1234";
        String number = generator.generateWithPrefix(prefix);

        assertThat(number).hasSize(16);
        assertThat(number).startsWith("1234");
        assertThat(number.substring(4)).containsOnlyDigits();
    }
}