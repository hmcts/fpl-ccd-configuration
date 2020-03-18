package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;

class CaseDetailsHelperTest {
    private static final String EXCEPTION_MESSAGE = "CCD Case number must be 16 digits long";

    @Test
    void shouldFormatCaseNumberWhen16DigitsProvided() {
        String formattedCaseNumber = formatCCDCaseNumber(1234123412341234L);
        assertThat(formattedCaseNumber).isEqualTo("1234-1234-1234-1234");
    }

    @Test
    void shouldThrowAnExceptionIfCaseNumberExceeds16Digits() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            formatCCDCaseNumber(12341234123412341L);
        });

        assertThat(exception.getMessage()).isEqualTo(EXCEPTION_MESSAGE);
    }

    @Test
    void shouldThrowAnExceptionIfCaseNumberIsLessThan16Digits() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            formatCCDCaseNumber(123412341234123L);
        });

        assertThat(exception.getMessage()).isEqualTo(EXCEPTION_MESSAGE);
    }

    @Test
    void shouldThrowAnExceptionIfCaseNumberIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            formatCCDCaseNumber(null);
        });

        assertThat(exception.getMessage()).isEqualTo(EXCEPTION_MESSAGE);
    }
}
