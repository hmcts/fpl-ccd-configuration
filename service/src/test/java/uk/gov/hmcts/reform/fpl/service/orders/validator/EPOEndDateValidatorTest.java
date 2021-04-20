package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_ORDER_DETAILS;

class EPOEndDateValidatorTest {

    private static final String INVALID_TIME = "Enter a valid time";
    private static final String FUTURE_DATE = "Enter an end date in the future";
    private static final String INVALID_END_DATE_RANGE = "Emergency protection orders cannot last longer than 8 days";

    private final Time time = new FixedTimeConfiguration().stoppedTime();

    private final EPOEndDateValidator underTest = new EPOEndDateValidator(time);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(EPO_ORDER_DETAILS);
    }

    @Test
    void validateFutureDate() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(time.now().minusDays(1))
                .manageOrdersEndDateTime(time.now().plusDays(1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(FUTURE_DATE));
    }

    @Test
    void validateMidnightTime() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(time.now().minusDays(2))
                .manageOrdersEndDateTime(LocalDateTime.of(time.now().minusDays(1).toLocalDate(), LocalTime.MIDNIGHT))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(INVALID_TIME));
    }

    @Test
    void validateEPOEndDateRangeNotInDateRange() {
        final LocalDateTime approvalDate = time.now().minusDays(10);
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(approvalDate)
                .manageOrdersEndDateTime(time.now().minusHours(1))
                .build())
            .build();

            assertThat(underTest.validate(caseData)).isEqualTo(List.of(INVALID_END_DATE_RANGE));
    }

    private static Stream<Arguments> epoEndDateRangeValues() {
        LocalDateTime approvalDate = LocalDateTime.now().minusDays(12);
        return Stream.of(
            Arguments.of("date before approval date", approvalDate, approvalDate.minusDays(4), false),
            Arguments.of("date not in expected range", approvalDate, approvalDate.plusDays(10), false),
            Arguments.of("valid date", approvalDate, approvalDate.plusDays(5), true)
        );
    }
}
