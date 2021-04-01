package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.time.LocalDateTime;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
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
            .hearingEndDate(pastDate)
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
            .hearingEndDate(pastDate.plusDays(1))
            .hearingOption(NEW_HEARING)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotThrowErrorsWhenPastHearingDateEnteredOnFirstHearing() {
        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDate(pastDate.plusHours(1))
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
            .hearingEndDate(dateWithInvalidTime)
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
            .hearingEndDate(pastDate.minusDays(1))
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
            .hearingEndDate(LocalDateTime.now().plusDays(1))
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
            .hearingEndDate(futureDate)
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
            .hearingEndDate(pastDate)
            .hearingOption(NEW_HEARING)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        Map<String, Object> responseData = callbackResponse.getData();
        assertThat(responseData.get("showConfirmPastHearingDatesPage").equals("Yes"));
        assertThat(responseData.get("startDateFlag").equals("Yes"));
        assertThat(responseData.get("endDateFlag").equals("Yes"));
        assertThat(responseData.get("hasSession").equals("Yes"));
    }

    @Test
    void shouldNotPopulateConfirmationHearingFieldsWhenHearingDateInFuture() {
        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(futureDate)
            .hearingEndDate(futureDate)
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
            .hearingEndDate(futureDate.plusMinutes(1))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }
}
