package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper.fromCCDMoneyGBP;
import static uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper.toCCDMoneyGBP;

class BigDecimalHelperTest {

    @Test
    void shouldReturnEmptyStringWhenAmountIsNull() {
        assertThat(toCCDMoneyGBP(null)).isEmpty();
    }

    @Test
    void shouldReturnAStringRepresentationOfTheAmountInPence() {
        assertThat(toCCDMoneyGBP(BigDecimal.valueOf(12.21))).isEqualTo("1221");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnNullWhenGivenNullOrEmptyString(String amount) {
        assertThat(fromCCDMoneyGBP(amount)).isEmpty();
    }

    @Test
    void shouldReturnNullWhenStringIsNotANumber() {
        assertThat(fromCCDMoneyGBP("1qazdert")).isEmpty();
    }

    @Test
    void shouldReturnBigDecimalRepresentationInMajorForm() {
        assertThat(fromCCDMoneyGBP("1221")).contains(BigDecimal.valueOf(12.21));
    }
}
