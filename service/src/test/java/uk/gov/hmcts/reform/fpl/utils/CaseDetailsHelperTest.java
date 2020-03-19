package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

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

    @Nested
    class RemoveTemporaryFields {
        private Map<String, Object> data = new HashMap<>();
        private CaseDetails caseDetails = CaseDetails.builder().data(data).build();

        @BeforeEach
        void populateMap() {
            data.put("key1", "some value 1");
            data.put("key2", "some value 2");
            data.put("key3", 3);
        }

        @AfterEach
        void clearMap() {
            data.clear();
        }

        @Test
        void shouldRemoveFieldsFromCaseDataMapWhenPresent() {
            removeTemporaryFields(caseDetails, "key1", "key2", "key3");

            assertThat(caseDetails.getData()).isEmpty();
        }

        @Test
        void shouldNotRemoveFieldsThatArePresentInMapWhenNotPassed() {
            removeTemporaryFields(caseDetails, "key1", "key3");

            assertThat(caseDetails.getData()).containsOnly(Map.entry("key2", "some value 2"));
        }
    }
}
