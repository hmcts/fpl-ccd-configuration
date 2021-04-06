package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE;

class ApprovalDateValidatorTest {

    private final ApprovalDateValidator underTest = new ApprovalDateValidator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(APPROVAL_DATE);
    }

    @Test
    void validate() {
        assertThat(underTest.validate(mock(CaseDetails.class))).isEqualTo(List.of());
    }
}
