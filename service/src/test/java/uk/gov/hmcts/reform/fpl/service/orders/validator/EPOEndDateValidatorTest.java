package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_EXPIRY_DATE;

class EPOEndDateValidatorTest {

    private final Time time = new FixedTimeConfiguration().stoppedTime();

    private final EPOEndDateValidator underTest = new EPOEndDateValidator(time);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(EPO_EXPIRY_DATE);
    }

    @Test
    void validateFutureDate() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(time.now())
                .manageOrdersEndDateTime(time.now().minusHours(1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of("Enter an end date in the future"));
    }

    @Test
    void validateMidnightTime() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(time.now())
                .manageOrdersEndDateTime(LocalDateTime.of(time.now().plusDays(1).toLocalDate(), LocalTime.MIDNIGHT))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of("Enter a valid time"));
    }

    @Test
    void validateEPOEndDateWhenDateIsNotInRange() {
        final LocalDateTime approvalDate = time.now();
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(approvalDate)
                .manageOrdersEndDateTime(approvalDate.plusDays(10))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(
            List.of("Emergency protection orders cannot last longer than 8 days"));
    }

    @Test
    void validateEPOEndDateWhenDateIsInRange() {
        final LocalDateTime approvalDate = time.now();
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(approvalDate)
                .manageOrdersEndDateTime(approvalDate.plusDays(1))
                .build())
            .build();

        Assertions.assertThat(underTest.validate(caseData)).isEmpty();
    }

    @Test
    void shouldNotValidateWhenApprovalDateIsNull() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(null)
                .manageOrdersEndDateTime(time.now().minusDays(1))
                .build())
            .build();

        Assertions.assertThat(underTest.validate(caseData)).isEmpty();
    }

    @Test
    void shouldNotValidateWhenEPOEndDateDateIsNull() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(null)
                .manageOrdersEndDateTime(null)
                .build())
            .build();

        Assertions.assertThat(underTest.validate(caseData)).isEmpty();
    }
}
