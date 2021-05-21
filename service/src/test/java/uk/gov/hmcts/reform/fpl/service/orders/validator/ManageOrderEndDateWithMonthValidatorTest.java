package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.util.Objects.deepEquals;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateTypeWithMonth.SET_CALENDAR_DAY;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateTypeWithMonth.SET_CALENDAR_DAY_AND_TIME;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateTypeWithMonth.SET_NUMBER_OF_MONTHS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.MANAGE_ORDER_END_DATE_WITH_MONTH;

class ManageOrderEndDateWithMonthValidatorTest {
    private static final String TEST_INVALID_TIME_MESSAGE = "Enter a valid time";
    private static final String TEST_FUTURE_DATE_MESSAGE = "Enter an end date in the future";
    private static final String TEST_END_DATE_RANGE_MESSAGE = "Supervision orders cannot last longer than 12 months";
    private static final String TEST_UNDER_DATE_RANGE_MESSAGE = "Supervision orders in months should be at least 1";

    private static final int MAXIMUM_MONTHS_ACCEPTED = 12;
    private static final int MINIMUM_MONTHS_ACCEPTED = 1;

    private final Time time = new FixedTimeConfiguration().stoppedTime();

    private final ManageOrderEndDateWithMonthValidator underTest = new ManageOrderEndDateWithMonthValidator(time);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(MANAGE_ORDER_END_DATE_WITH_MONTH);
    }

    @Test
    void shouldAcceptDateAfterTodayAndLessThenOneYearFromNow() {
        LocalDate tomorrowDate = time.now().plusDays(1).toLocalDate();

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_CALENDAR_DAY)
                .manageOrdersSetDateEndDate(tomorrowDate)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptDateTimeAfterTodayAndLessThenOneYearFromNow() {
        LocalDateTime tomorrowDateTime = time.now().plusDays(1).plusHours(2).plusMinutes(30);

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(tomorrowDateTime)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptMonthsSelectedWhenBelowBoundary() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(MAXIMUM_MONTHS_ACCEPTED - 1)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptMonthsSelectedWhenOnBoundary() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(MAXIMUM_MONTHS_ACCEPTED)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptDateTimeOnBoundaryOfMaxAllowedEndDateTime() {
        LocalDateTime onBoundaryDateTime = time.now().plusYears(1).plusSeconds(0);

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(onBoundaryDateTime)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldRejectDateTimeOverBoundaryOfMaxAllowedEndDateTime() {
        LocalDateTime overBoundaryDateTime = time.now().plusYears(1).plusSeconds(1);

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(overBoundaryDateTime)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_END_DATE_RANGE_MESSAGE));
    }

    @Test
    void shouldReturnErrorForDateOverOneYearFromNow() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_CALENDAR_DAY)
                .manageOrdersSetDateEndDate(dateNow().plusYears(2))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_END_DATE_RANGE_MESSAGE));
    }

    @Test
    void shouldReturnErrorForDateTimeOverOneYearFromNo() {
        final LocalDateTime supervisionOrderEndDateTime = time.now();
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(supervisionOrderEndDateTime.plusMonths(MAXIMUM_MONTHS_ACCEPTED + 1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(
            List.of(TEST_END_DATE_RANGE_MESSAGE));
    }

    @Test
    void shouldReturnErrorForDateInPast() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_CALENDAR_DAY)
                .manageOrdersSetDateEndDate(dateNow().minusDays(7))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_FUTURE_DATE_MESSAGE));
    }

    @Test
    void shouldReturnErrorForDateTimeInPast() {
        final LocalDateTime supervisionOrderEndDateTime = time.now();
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(supervisionOrderEndDateTime.minusDays(10))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_FUTURE_DATE_MESSAGE));
    }

    @Test
    void shouldReturnErrorForNumberOfMonthsOverMaximum() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(MAXIMUM_MONTHS_ACCEPTED + 1)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_END_DATE_RANGE_MESSAGE));
    }

    @Test
    void shouldReturnErrorForNumberOfMonthsIsLessThanAccepted() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_NUMBER_OF_MONTHS)
                .manageOrdersSetMonthsEndDate(MINIMUM_MONTHS_ACCEPTED - 1)
                .build())
            .build();

        deepEquals(underTest.validate(caseData), TEST_UNDER_DATE_RANGE_MESSAGE);
    }

    @Test
    void shouldReturnErrorWhenInvalidTimeFormatIsEntered() {
        final LocalDateTime invalidTime = dateNow().plusDays(1).atTime(LocalTime.MIDNIGHT);

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEndDateTypeWithMonth(SET_CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(invalidTime)
                .build())
            .build();
        assertThat(underTest.validate(caseData)).isEqualTo(List.of(TEST_INVALID_TIME_MESSAGE));
    }

    private LocalDate dateNow() {
        return time.now().toLocalDate();
    }
}
