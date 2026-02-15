package com.nastian.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardNumberMaskerTest {

    private CardNumberMasker masker;

    @BeforeEach
    void setUp() {
        masker = new CardNumberMasker();
    }

    @Test
    @DisplayName("Маскирование 16-значного номера")
    void mask_ValidNumber_ReturnsMasked() {
        String masked = masker.mask("1234567890123456");

        assertThat(masked).isEqualTo("**** **** **** 3456");
    }

    @Test
    @DisplayName("Маскирование короткого номера")
    void mask_ShortNumber_ReturnsMasked() {
        String masked = masker.mask("123");

        assertThat(masked).isEqualTo("****");
    }

    @Test
    @DisplayName("Маскирование null")
    void mask_NullNumber_ReturnsMasked() {
        String masked = masker.mask(null);

        assertThat(masked).isEqualTo("****");
    }

    @Test
    @DisplayName("Маскирование с кастомными символами")
    void mask_CustomChars_ReturnsMasked() {
        String masked = masker.mask("1234567890123456", "#", "-");

        assertThat(masked).isEqualTo("####-####-####-3456");
    }
}