package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("integration-test")
@OverrideAutoConfiguration(enabled = true)
@WebMvcTest(ManageHearingsController.class)
public class ManageHearingsControllerValidateHearingDatesMidEventTest extends AbstractControllerTest {

    private static final String CASE_ID = "12345";

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private Time time;


    ManageHearingsControllerValidateHearingDatesMidEventTest() {
        super("manage-hearings");
    }

    @ParameterizedTest
    @ValueSource(strings = {"EDIT_HEARING", "ADJOURN_HEARING", "VACATE_HEARING"})
    void shouldThrowErrorsWhenInvalidHearingDatesEnteredOnHearingOption(String hearingOption) {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", time.now().minusDays(1),
                "hearingEndDate", time.now().minusDays(2),
                "hearingOption", hearingOption))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "Enter a start date in the future",
            "Enter an end date in the future");
    }

    @Test
    void shouldThrowErrorsWhenInvalidHearingDatesEnteredOnAddHearingIfToggledOff() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(false);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", time.now().minusDays(1),
                "hearingEndDate", time.now().minusDays(2),
                "hearingOption","NEW_HEARING"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "Enter a start date in the future",
            "Enter an end date in the future");
    }

    @Test
    void shouldNotThrowErrorsWhenPastHearingDatesEnteredOnAddHearingIfToggledOn() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", time.now().minusDays(1),
                "hearingEndDate", time.now().minusDays(2),
                "hearingOption","NEW_HEARING"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotThrowErrorsWhenPastHearingDateEnteredOnFirstHearingAddedAndToggledOn() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", time.now().minusDays(1),
                "hearingEndDate", time.now().minusDays(2)))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldThrowOnlyInvalidTimeErrorsWhenPastHearingDatesEnteredOnAddHearingIfToggledOn() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", time.now().minusDays(1),
                "hearingEndDate", LocalDateTime.of(1990, 10, 2, 00, 00),
                "hearingOption","NEW_HEARING"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "Enter a valid end time");
    }

    @Test
    void shouldPopulateConfirmationHearingFieldsWhenHearingDateInPastAndToggledOn() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", time.now().minusDays(1),
                "hearingEndDate", time.now().minusDays(1),
                "hearingOption","NEW_HEARING"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        Map<String, Object> responseData = callbackResponse.getData();
        assertThat(responseData.get("pageShow").equals("Yes"));
        assertThat(responseData.get("showStartDateLabel").equals("Yes"));
        assertThat(responseData.get("showEndDateLabel").equals("Yes"));
    }

    @Test
    void shouldNotPopulateConfirmationHearingFieldsWhenHearingDateInPastAndToggledOff() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(false);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", time.now().minusDays(1),
                "hearingEndDate", time.now().minusDays(1),
                "hearingOption","NEW_HEARING"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData).doesNotContainKeys("pageShow", "showStartDateLabel", "showEndDateLabel");
    }

    @Test
    void shouldNotPopulateConfirmationHearingFieldsWhenHearingDateInFutureAndToggledOn() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", time.now().plusDays(1),
                "hearingEndDate", time.now().plusDays(1),
                "hearingOption","NEW_HEARING"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData).doesNotContainKeys("pageShow", "showStartDateLabel", "showEndDateLabel");
    }

    @Test
    void shouldNotThrowWhenValidHearingDatesEntered() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", time.now().plusDays(1), "hearingEndDate", time.now().plusDays(2)))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }
}
