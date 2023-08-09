package uk.gov.hmcts.reform.fpl.controllers.listgatekeepinghearing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ListGatekeepingHearingController;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.HOURS_MINS;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@OverrideAutoConfiguration(enabled = true)
@WebMvcTest(ListGatekeepingHearingController.class)
class ListGatekeepingHearingControllerValidateHearingDatesMidEventTest extends AbstractCallbackTest {

    private static final LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
    private static final LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

    ListGatekeepingHearingControllerValidateHearingDatesMidEventTest() {
        super("list-gatekeeping-hearing");
    }

    //@Test
    void shouldNotThrowErrorsWhenPastHearingDatesEnteredOnAddHearing() {

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDateTime(pastDate.plusDays(1))
            .hearingDuration(DATE_TIME.getType())
            .hearingOption(NEW_HEARING)
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    //@Test
    void shouldNotThrowErrorsWhenDaysAddedOnAddHearing() {

        final LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        final int numberOfDays = 10;
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(startDate)
            .hearingDays(numberOfDays)
            .hearingDuration(DAYS.getType())
            .hearingOption(NEW_HEARING)
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");
        final Map<String, Object> responseData = callbackResponse.getData();
        assertThat(callbackResponse.getErrors()).isEmpty();
        assertThat(responseData.get("hearingEndDate")).isNotNull();
        assertThat(responseData.get("showConfirmPastHearingDatesPage")).isEqualTo(YES.getValue());
        assertThat(responseData.get("startDateFlag")).isEqualTo(YES.getValue());
        assertThat(responseData.get("endDateFlag")).isEqualTo(YES.getValue());
    }

    //@Test
    void shouldThrowErrorsWhenDaysSetToZero() {

        final LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.NOON);

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(startDate)
            .hearingDuration(DAYS.getType())
            .hearingDays(0)
            .hearingOption(NEW_HEARING)
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");
        assertThat(callbackResponse.getErrors()).containsExactly(
            "Enter valid days");
    }


    //@Test
    void shouldNotThrowErrorsWhenHoursAndMinsAddedOnAddHearing() {

        final LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.NOON);
        final int hours = 10;
        final int minutes = 30;
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(startDate)
            .hearingDuration(HOURS_MINS.getType())
            .hearingHours(hours)
            .hearingMinutes(minutes)
            .hearingOption(NEW_HEARING)
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");
        final Map<String, Object> responseData = callbackResponse.getData();
        assertThat(callbackResponse.getErrors()).isEmpty();
        assertThat(responseData.get("hearingEndDate")).isNotNull();
        final LocalDateTime hearingEndDate = LocalDateTime.parse((String) responseData.get("hearingEndDate"));
        final LocalDateTime expectedEndDate = startDate.plusHours(hours).plusMinutes(minutes);
        assertThat(hearingEndDate).isEqualTo(expectedEndDate);
        assertThat(responseData.get("showConfirmPastHearingDatesPage")).isEqualTo(YES.getValue());
        assertThat(responseData.get("startDateFlag")).isEqualTo(YES.getValue());
        assertThat(responseData.get("endDateFlag")).isEqualTo(YES.getValue());
    }

    //@Test
    void shouldThrowErrorsWhenHoursAndMinsAreSetToZero() {

        final LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.NOON);

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(startDate)
            .hearingDuration(HOURS_MINS.getType())
            .hearingHours(0)
            .hearingMinutes(0)
            .hearingOption(NEW_HEARING)
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");
        assertThat(callbackResponse.getErrors()).containsExactly(
            "Enter valid hours and minutes");
    }


    //@Test
    void shouldNotThrowErrorsWhenPastHearingDateEnteredOnFirstHearing() {

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDateTime(pastDate.plusHours(1))
            .hearingDuration(DATE_TIME.getType())
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    //@Test
    void shouldThrowOnlyInvalidTimeErrorsWhenPastHearingDatesEnteredOnAddHearing() {

        final LocalDateTime dateWithInvalidTime = LocalDateTime.of(1990, 10, 2, 0, 0);

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDateTime(dateWithInvalidTime)
            .hearingDuration(DATE_TIME.getType())
            .hearingOption(NEW_HEARING)
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "Enter a valid end time");
    }

    //@Test
    void shouldThrowInvalidHearingEndDateTimeErrorWhenAddingAHearingWithPastDate() {

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDateTime(pastDate.minusDays(1))
            .hearingDuration(DATE_TIME.getType())
            .hearingOption(NEW_HEARING)
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsOnly(
            "The end date and time must be after the start date and time");
    }

    //@Test
    void shouldThrowInvalidHearingEndTimeErrorWhenHearingEndDateIsBeforeStartDate() {

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(LocalDateTime.now().plusDays(2))
            .hearingEndDateTime(LocalDateTime.now().plusDays(1))
            .hearingDuration(DATE_TIME.getType())
            .hearingOption(NEW_HEARING)
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsOnly(
            "The end date and time must be after the start date and time");
    }

    //@Test
    void shouldThrowInvalidHearingEndTimeErrorWhenHearingEndTimeIsSameAsStartTime() {

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(futureDate)
            .hearingDuration(DATE_TIME.getType())
            .hearingEndDateTime(futureDate)
            .hearingOption(NEW_HEARING)
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsOnly(
            "The end date and time must be after the start date and time");
    }

    //@Test
    void shouldPopulateConfirmationHearingFieldsWhenHearingDateInPast() {

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingDuration(DATE_TIME.getType())
            .hearingEndDateTime(pastDate)
            .hearingOption(NEW_HEARING)
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        final Map<String, Object> responseData = callbackResponse.getData();
        assertThat(responseData.get("showConfirmPastHearingDatesPage")).isEqualTo(YES.getValue());
        assertThat(responseData.get("startDateFlag")).isEqualTo(YES.getValue());
        assertThat(responseData.get("endDateFlag")).isEqualTo(YES.getValue());
    }

    //@Test
    void shouldNotPopulateConfirmationHearingFieldsWhenHearingDateInFuture() {

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(futureDate)
            .hearingDuration(DATE_TIME.getType())
            .hearingEndDateTime(futureDate)
            .hearingOption(NEW_HEARING)
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        final Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData).doesNotContainKeys("startDateFlag", "endDateFlag");
        assertThat(responseData).containsEntry("showConfirmPastHearingDatesPage", NO.getValue());
    }

    //@Test
    void shouldNotThrowErrorsWhenValidHearingDatesEntered() {

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(futureDate)
            .hearingDuration(DATE_TIME.getType())
            .hearingEndDateTime(futureDate.plusMinutes(1))
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    //@Test
    void shouldThrowErrorsWhenInvalidHearingDurationEntered() {

        final CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("hearingDays", "0.75"))
            .build();
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(
            caseDetails,
            "validate-hearing-dates");

        assertThat(callbackResponse.getErrors())
            .containsExactlyInAnyOrder("Hearing length, in days should be a whole number");
    }
}
