package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;

class CaseDetailsHelperTest {
    @Test
    void shouldFormatCaseNumberWhen16DigitsProvided() {
        String formattedCaseNumber = formatCCDCaseNumber(1234123412341234L);
        assertThat(formattedCaseNumber).isEqualTo("1234-1234-1234-1234");
    }

    @Test
    void shouldFormatCaseNumberWhen9DigitsProvided() {
        String formattedCaseNumber = formatCCDCaseNumber(123412341L);
        assertThat(formattedCaseNumber).isEqualTo("1234-1234-1");
    }
}
