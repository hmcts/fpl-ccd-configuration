package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;

@ActiveProfiles("integration-test")
@OverrideAutoConfiguration(enabled = true)
@WebMvcTest(ManageHearingsController.class)
class ManageHearingsControllerValidateHearingDatesMidEventTest extends AbstractControllerTest {
    @MockBean
    private FeatureToggleService featureToggleService;

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
    void shouldThrowErrorsWhenInvalidHearingDatesEnteredOnAddHearingAndPastHearingDatesIsDisabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(false);

        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDate(pastDate)
            .hearingOption(NEW_HEARING)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "Enter a start date in the future",
            "Enter an end date in the future");
    }

    @Test
    void shouldNotThrowErrorsWhenPastHearingDatesEnteredOnAddHearingAndPastHearingDatesIsEnabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDate(pastDate)
            .hearingOption(NEW_HEARING)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotThrowErrorsWhenPastHearingDateEnteredOnFirstHearingAndPastHearingDatesIsEnabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDate(pastDate)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldThrowOnlyInvalidTimeErrorsWhenPastHearingDatesEnteredOnAddHearingAndPastHearingDatesIsEnabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        LocalDateTime dateWithInvalidTime = LocalDateTime.of(1990, 10, 2, 00, 00);

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
    void shouldPopulateConfirmationHearingFieldsWhenHearingDateInPastAndPastHearingDatesIsEnabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

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
    }

    @Test
    void shouldNotPopulateConfirmationHearingFieldsWhenHearingDateInPastAndPastHearingDatesIsDisabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(false);

        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .hearingStartDate(pastDate)
            .hearingEndDate(pastDate)
            .hearingOption(NEW_HEARING)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData).doesNotContainKeys("showConfirmPastHearingDatesPage", "startDateFlag", "endDateFlag");
    }

    @Test
    void shouldNotPopulateConfirmationHearingFieldsWhenHearingDateInFutureAndPastHearingDatesIsEnabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

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
            .hearingEndDate(futureDate)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }
}
