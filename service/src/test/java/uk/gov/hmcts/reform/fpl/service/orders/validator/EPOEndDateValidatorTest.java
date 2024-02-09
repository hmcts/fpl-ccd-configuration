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
import static uk.gov.hmcts.reform.fpl.service.orders.validator.EPOEndDateValidator.BEFORE_APPROVAL_MESSAGE;
import static uk.gov.hmcts.reform.fpl.service.orders.validator.EPOEndDateValidator.END_DATE_RANGE_MESSAGE;

class EPOEndDateValidatorTest {

    private final Time time = new FixedTimeConfiguration().stoppedTime();

    private final EPOEndDateValidator underTest = new EPOEndDateValidator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(EPO_EXPIRY_DATE);
    }

    @Test
    void validateFutureDate() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(time.now().minusDays(1))
                .manageOrdersEndDateTime(time.now().plusDays(30))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).asList().isEmpty();
    }

    @Test
    void validateDatBeforeToday() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(time.now().minusDays(3))
                .manageOrdersEndDateTime(time.now().minusDays(1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).asList().isEmpty();
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
                .manageOrdersEndDateTime(approvalDate.plusYears(1).plusSeconds(1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(END_DATE_RANGE_MESSAGE));
    }

    @Test
    void validateEPOEndDateWhenDateIsInRange() {
        final LocalDateTime approvalDate = time.now();
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(approvalDate)
                .manageOrdersEndDateTime(approvalDate.plusDays(1).minusSeconds(1))
                .build())
            .build();

        Assertions.assertThat(underTest.validate(caseData)).isEmpty();
    }

    @Test
    void validateEPOEndDateWhenDateBeforeApproval() {
        final LocalDateTime approvalDate = time.now().plusDays(10);
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(approvalDate)
                .manageOrdersEndDateTime(approvalDate.minusDays(1).minusSeconds(1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(BEFORE_APPROVAL_MESSAGE));
    }
}
