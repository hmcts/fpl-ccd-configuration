package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.deepEquals;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateTypeWithMonth.CALENDAR_DAY;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateTypeWithMonth.CALENDAR_DAY_AND_TIME;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateTypeWithMonth.NUMBER_OF_MONTHS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.MANAGE_ORDER_END_DATE_WITH_MONTH;

class ManageOrderEndDateWithMonthValidatorTest {
    private static final String TEST_AFTER_APPROVAL_DATE_MESSAGE = "Enter an end date after the approval date";
    private static final String TEST_END_DATE_RANGE_MESSAGE = "This order cannot last longer than 12 months";
    private static final String TEST_UNDER_DATE_RANGE_MESSAGE = "This order must last for at least 1 month";

    private static final int MAXIMUM_MONTHS_ACCEPTED = 12;
    private static final int MINIMUM_MONTHS_ACCEPTED = 1;

    private final Time time = new FixedTimeConfiguration().stoppedTime();
    private final LocalDate todayDate = time.now().toLocalDate();
    private final LocalDate tomorrowDate = time.now().plusDays(1).toLocalDate();
    private final LocalDate approvalDate = todayDate;
    private final LocalDateTime approvalDateTime = time.now();

    private final ManageOrderEndDateWithMonthValidator underTest = new ManageOrderEndDateWithMonthValidator(time);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(MANAGE_ORDER_END_DATE_WITH_MONTH);
    }

    // Date
    @Test
    void shouldAcceptOrderDateWhenWithinAcceptableRange() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY)
                .manageOrdersSetDateEndDate(todayDate.plusMonths(6))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptOrderDateWhenOnEarliestBoundary() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY)
                .manageOrdersSetDateEndDate(tomorrowDate)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptOrderDateWhenOnHighestBoundary() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY)
                .manageOrdersSetDateEndDate(approvalDate.plusMonths(MAXIMUM_MONTHS_ACCEPTED).minusDays(1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldReturnErrorForOrderDateWhenBelowLowestBoundary() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY)
                .manageOrdersSetDateEndDate(approvalDate.minusDays(1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_AFTER_APPROVAL_DATE_MESSAGE));
    }

    @Test
    void shouldReturnErrorForOrderDateWhenAboveHighestBoundary() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY)
                .manageOrdersSetDateEndDate(approvalDate.plusMonths(MAXIMUM_MONTHS_ACCEPTED + 1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_END_DATE_RANGE_MESSAGE));
    }

    // DateTime
    @Test
    void shouldAcceptDateTimeOnBoundaryOfMaxAllowedEndDateTime() {
        LocalDateTime onBoundaryDateTime = approvalDateTime
            .plusMonths(MAXIMUM_MONTHS_ACCEPTED)
            .toLocalDate()
            .atStartOfDay();

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(onBoundaryDateTime)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptDateTimeOnBoundaryOfMinAllowedEndDateTime() {
        LocalDateTime onBoundaryDateTime = approvalDateTime
            .plusMonths(MINIMUM_MONTHS_ACCEPTED)
            .toLocalDate()
            .atStartOfDay();

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(onBoundaryDateTime)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptOrderDateTimeWhenWithinAcceptableRange() {
        LocalDateTime onBoundaryDateTime = approvalDateTime.plusMonths(6);

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(onBoundaryDateTime)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptOrderDateTimeWhenOnLowestBoundary() {
        LocalDateTime onBoundaryDateTime = approvalDate.plusMonths(1).atTime(0,0,1);

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(onBoundaryDateTime)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptOrderDateTimeWhenOnHighestBoundary() {
        LocalDateTime endDate = approvalDate
            .atTime(23,59,59)
            .plusMonths(MAXIMUM_MONTHS_ACCEPTED)
            .minusDays(1);

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(endDate)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldReturnErrorForOrderDateTimeWhenBelowEarliestBoundary() {
        LocalDateTime endDate = approvalDate.atTime(0,0,1);

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(endDate.minusSeconds(2))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_AFTER_APPROVAL_DATE_MESSAGE));
    }

    @Test
    void shouldReturnErrorForOrderDateTimeWhenAboveLatestBoundary() {
        LocalDateTime endDate = approvalDate.atTime(23,59,59).plusMonths(MAXIMUM_MONTHS_ACCEPTED);

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(endDate.plusSeconds(2))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_END_DATE_RANGE_MESSAGE));
    }

    @Test
    void shouldReturnErrorForOrderDateTimeWhenBelowLowestBoundary() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(approvalDateTime.minusMonths(4))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_AFTER_APPROVAL_DATE_MESSAGE));
    }

    // Month
    @Test
    void shouldAcceptMonthsSelectedWhenOnBoundary() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(approvalDate)
                .manageOrdersEndDateTypeWithMonth(NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(MAXIMUM_MONTHS_ACCEPTED)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptMonthsSelectedWhenBelowHighestBoundaryMaximum() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(MAXIMUM_MONTHS_ACCEPTED - 1)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptMonthsSelectedWhenOnLowestBoundaryMinimum() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(MINIMUM_MONTHS_ACCEPTED)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptMonthsSelectedWhenAboveLowestBoundaryMinimum() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(MINIMUM_MONTHS_ACCEPTED + 1)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldReturnErrorForNumberOfMonthsOverMaximumBoundary() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(MAXIMUM_MONTHS_ACCEPTED + 1)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_END_DATE_RANGE_MESSAGE));
    }

    @Test
    void shouldReturnErrorForNumberOfMonthsUnderBoundaryMinimum() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(MINIMUM_MONTHS_ACCEPTED - 1)
                .build())
            .build();

        deepEquals(underTest.validate(caseData), TEST_UNDER_DATE_RANGE_MESSAGE);
    }
}
