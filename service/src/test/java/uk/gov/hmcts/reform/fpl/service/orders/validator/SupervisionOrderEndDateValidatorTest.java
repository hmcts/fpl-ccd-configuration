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
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.SUPERVISION_ORDER_END_DATE;

class SupervisionOrderEndDateValidatorTest {

    private static final String TEST_INVALID_TIME_MESSAGE = "Enter a valid time";
    private static final String TEST_FUTURE_DATE_MESSAGE = "Enter an end date in the future";
    private static final String TEST_END_DATE_RANGE_MESSAGE = "Supervision orders cannot last longer than 12 months";

    private final Time time = new FixedTimeConfiguration().stoppedTime();

    private final SupervisionOrderEndDateValidator underTest = new SupervisionOrderEndDateValidator(time);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(SUPERVISION_ORDER_END_DATE);
    }

    @Test
    void validateFutureDate() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(time.now())
                .manageOrdersEndDateTime(time.now().minusHours(1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_FUTURE_DATE_MESSAGE));
    }

    @Test
    void validateMidnightTime() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(time.now())
                .manageOrdersEndDateTime(LocalDateTime.of(time.now().plusDays(1).toLocalDate(), LocalTime.MIDNIGHT))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_INVALID_TIME_MESSAGE));
    }

    @Test
    void validateEndDateWhenDateIsNotInRange() {
        final LocalDateTime approvalDate = time.now();
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(approvalDate)
                .manageOrdersEndDateTime(approvalDate.plusMonths(13))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(
            List.of(TEST_END_DATE_RANGE_MESSAGE));
    }

    @Test
    void validateEndDateWhenDateIsInRange() {
        final LocalDateTime approvalDate = time.now();
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(approvalDate)
                .manageOrdersEndDateTime(approvalDate.plusDays(1))
                .build())
            .build();

        Assertions.assertThat(underTest.validate(caseData)).isEmpty();
    }
}
