package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
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

@OverrideAutoConfiguration(enabled = true)
@WebMvcTest(ManageHearingsController.class)
class ManageHearingsControllerValidateHearingDatesMidEventTest extends AbstractCallbackTest {
    private static LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
    private static LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

    ManageHearingsControllerValidateHearingDatesMidEventTest() {
        super("manage-hearings");
    }

    @ParameterizedTest
    @EnumSource(value = HearingOptions.class, names = {"EDIT_HEARING", "ADJOURN_HEARING", "VACATE_HEARING"})
    void shouldThrowErrorsWhenInvalidHearingDatesEnteredOnHearingOption(HearingOptions hearingOption) {
        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDateTime(pastDate)
            .hearingDuration(DATE_TIME.getType())
            .hearingOption(hearingOption)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "Enter a start date in the future",
            "Enter an end date in the future");
    }


    @Test
    void shouldNotThrowErrorsWhenPastHearingDatesEnteredOnAddHearing() {
        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDateTime(pastDate.plusDays(1))
            .hearingDuration(DATE_TIME.getType())
            .hearingOption(NEW_HEARING)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotThrowErrorsWhenDaysAddedOnAddHearing() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        int numberOfDays = 10;
        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(startDate)
            .hearingDays(String.valueOf(numberOfDays))
            .hearingDuration(DAYS.getType())
            .hearingOption(NEW_HEARING)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");
        Map<String, Object> responseData = callbackResponse.getData();
        assertThat(callbackResponse.getErrors()).isEmpty();
        assertThat(responseData.get("hearingEndDate")).isNotNull();
        LocalDateTime hearingEndDate = LocalDateTime.parse((String) responseData.get("hearingEndDate"));
        assertThat(hearingEndDate).isEqualTo(startDate.plusDays(numberOfDays));
        assertThat(responseData.get("showConfirmPastHearingDatesPage")).isEqualTo("Yes");
        assertThat(responseData.get("startDateFlag")).isEqualTo("Yes");
        assertThat(responseData.get("endDateFlag")).isEqualTo("Yes");
        assertThat(responseData.get("hasSession")).isEqualTo("Yes");
    }

    @Test
    void shouldNotThrowErrorsWhenHoursAndMinsAddedOnAddHearing() {
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.NOON);
        int hours = 10;
        int minutes = 30;
        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(startDate)
            .hearingDuration(HOURS_MINS.getType())
            .hearingHours(String.valueOf(hours))
            .hearingMinutes(String.valueOf(minutes))
            .hearingOption(NEW_HEARING)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");
        Map<String, Object> responseData = callbackResponse.getData();
        assertThat(callbackResponse.getErrors()).isEmpty();
        assertThat(responseData.get("hearingEndDate")).isNotNull();
        LocalDateTime hearingEndDate = LocalDateTime.parse((String) responseData.get("hearingEndDate"));
        LocalDateTime expectedEndDate = startDate.plusHours(hours).plusMinutes(minutes);
        assertThat(hearingEndDate).isEqualTo(expectedEndDate);
        assertThat(responseData.get("showConfirmPastHearingDatesPage")).isEqualTo("Yes");
        assertThat(responseData.get("startDateFlag")).isEqualTo("Yes");
        assertThat(responseData.get("endDateFlag")).isEqualTo("Yes");
        assertThat(responseData.get("hasSession")).isEqualTo("Yes");
    }

    @Test
    void shouldNotThrowErrorsWhenPastHearingDateEnteredOnFirstHearing() {
        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDateTime(pastDate.plusHours(1))
            .hearingDuration(DATE_TIME.getType())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldThrowOnlyInvalidTimeErrorsWhenPastHearingDatesEnteredOnAddHearing() {
        LocalDateTime dateWithInvalidTime = LocalDateTime.of(1990, 10, 2, 0, 0);

        CaseData caseDetails = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDateTime(dateWithInvalidTime)
            .hearingDuration(DATE_TIME.getType())
            .hearingOption(NEW_HEARING)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "Enter a valid end time");
    }

    @Test
    void shouldThrowInvalidHearingEndDateTimeErrorWhenAddingAHearingWithPastDate() {
        CaseData caseDetails = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDateTime(pastDate.minusDays(1))
            .hearingDuration(DATE_TIME.getType())
            .hearingOption(NEW_HEARING)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsOnly(
            "The end date and time must be after the start date and time");
    }

    @ParameterizedTest
    @EnumSource(value = HearingOptions.class, names = {"EDIT_HEARING", "NEW_HEARING"})
    void shouldThrowInvalidHearingEndTimeErrorWhenHearingEndDateIsBeforeStartDate(HearingOptions hearingOptions) {
        CaseData caseDetails = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(LocalDateTime.now().plusDays(2))
            .hearingEndDateTime(LocalDateTime.now().plusDays(1))
            .hearingDuration(DATE_TIME.getType())
            .hearingOption(hearingOptions)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsOnly(
            "The end date and time must be after the start date and time");
    }

    @ParameterizedTest
    @EnumSource(value = HearingOptions.class, names = {"EDIT_HEARING", "NEW_HEARING"})
    void shouldThrowInvalidHearingEndTimeErrorWhenHearingEndTimeIsSameAsStartTime(HearingOptions hearingOptions) {
        CaseData caseDetails = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(futureDate)
            .hearingDuration(DATE_TIME.getType())
            .hearingEndDateTime(futureDate)
            .hearingOption(hearingOptions)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsOnly(
            "The end date and time must be after the start date and time");
    }

    @Test
    void shouldPopulateConfirmationHearingFieldsWhenHearingDateInPast() {
        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingDuration(DATE_TIME.getType())
            .hearingEndDateTime(pastDate)
            .hearingOption(NEW_HEARING)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        Map<String, Object> responseData = callbackResponse.getData();
        assertThat(responseData.get("showConfirmPastHearingDatesPage")).isEqualTo("Yes");
        assertThat(responseData.get("startDateFlag")).isEqualTo("Yes");
        assertThat(responseData.get("endDateFlag")).isEqualTo("Yes");
        assertThat(responseData.get("hasSession")).isEqualTo("Yes");
    }

    @Test
    void shouldNotPopulateConfirmationHearingFieldsWhenHearingDateInFuture() {
        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(futureDate)
            .hearingDuration(DATE_TIME.getType())
            .hearingEndDateTime(futureDate)
            .hearingOption(NEW_HEARING)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData).doesNotContainKeys("startDateFlag", "endDateFlag");
        assertThat(responseData).containsEntry("showConfirmPastHearingDatesPage", "No");
    }

    @Test
    void shouldNotThrowErrorsWhenValidHearingDatesEntered() {
        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(futureDate)
            .hearingDuration(DATE_TIME.getType())
            .hearingEndDateTime(futureDate.plusMinutes(1))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }
}
