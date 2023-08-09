package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons.CLARIFICATION_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons.INCOMPLETE;

class ReturnApplicationTest {

    @Test
    void shouldFormatReturnReasonsWhenMultipleReasonsAreGiven() {
        ReturnApplication returnApplication = buildReturnApplication(INCOMPLETE, CLARIFICATION_NEEDED);
        assertThat(returnApplication.getFormattedReturnReasons()).isEqualTo("Application incomplete,"
            + " clarification needed");
    }

    @Test
    void shouldFormatReturnReasonsWhenASingleReasonIsGiven() {
        ReturnApplication returnApplication = buildReturnApplication(INCOMPLETE);
        assertThat(returnApplication.getFormattedReturnReasons()).isEqualTo("Application incomplete");
    }

    @Test
    void shouldReturnAnEmptyStringWhenNoReturnReasonsAreProvided() {
        ReturnApplication returnApplication = ReturnApplication.builder().build();
        assertThat(returnApplication.getFormattedReturnReasons()).isEmpty();
    }

    private ReturnApplication buildReturnApplication(ReturnedApplicationReasons... reasons) {
        return ReturnApplication.builder().reason(List.of(reasons)).build();
    }
}
