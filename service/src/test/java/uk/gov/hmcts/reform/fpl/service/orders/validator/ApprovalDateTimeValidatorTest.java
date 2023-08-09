package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE_TIME;

class ApprovalDateTimeValidatorTest {

    private static final String MESSAGE = "Approval date cannot be in the future";

    private final Time time = new FixedTimeConfiguration().stoppedTime();

    private final ApprovalDateTimeValidator underTest = new ApprovalDateTimeValidator(time);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(APPROVAL_DATE_TIME);
    }

    @Test
    void validateCurrentTime() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(time.now())
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void validatePast() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(time.now().minusMinutes(1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void validateFuture() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(time.now().plusHours(1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(MESSAGE));
    }
}
