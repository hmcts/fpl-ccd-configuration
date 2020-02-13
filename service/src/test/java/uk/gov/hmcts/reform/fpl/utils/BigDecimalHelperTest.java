package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BigDecimalHelperTest {

    @Test
    void shouldReturnEmptyStringWhenAmountIsNull() {
        assertThat(BigDecimalHelper.toCCDMoneyGBP(null)).isEmpty();
    }

    @Test
    void shouldReturnAStringRepresentationOfTheAmountInPence() {
        assertThat(BigDecimalHelper.toCCDMoneyGBP(BigDecimal.valueOf(12.21))).isEqualTo("1221");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnNullWhenGivenNullOrEmptyString(String amount) {
        assertThat(BigDecimalHelper.fromCCDMoneyGBP(amount)).isNull();
    }

    @Test
    void shouldReturnNullWhenStringIsNotANumber() {
        assertThat(BigDecimalHelper.fromCCDMoneyGBP("1qazdert")).isNull();
    }

    @Test
    void shouldReturnBigDecimalRepresentationInMajorForm() {
        assertThat(BigDecimalHelper.fromCCDMoneyGBP("1221")).isEqualTo(BigDecimal.valueOf(12.21));
    }
}
