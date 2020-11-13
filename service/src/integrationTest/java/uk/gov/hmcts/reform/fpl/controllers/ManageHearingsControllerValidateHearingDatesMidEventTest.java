package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("integration-test")
@OverrideAutoConfiguration(enabled = true)
@WebMvcTest(ManageHearingsController.class)
public class ManageHearingsControllerValidateHearingDatesMidEventTest extends AbstractControllerTest {
    @MockBean
    private FeatureToggleService featureToggleService;

    private static final String CASE_ID = "12345";
    private static LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
    private static LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

    ManageHearingsControllerValidateHearingDatesMidEventTest() {
        super("manage-hearings");
    }

    @ParameterizedTest
    @ValueSource(strings = {"EDIT_HEARING", "ADJOURN_HEARING", "VACATE_HEARING"})
    void shouldThrowErrorsWhenInvalidHearingDatesEnteredOnHearingOption(String hearingOption) {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate,
                "hearingOption", hearingOption))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "Enter a start date in the future",
            "Enter an end date in the future");
    }

    @Test
    void shouldThrowErrorsWhenInvalidHearingDatesEnteredOnAddHearingAndPastHearingDatesIsDisabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(false);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate,
                "hearingOption","NEW_HEARING"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "Enter a start date in the future",
            "Enter an end date in the future");
    }

    @Test
    void shouldNotThrowErrorsWhenPastHearingDatesEnteredOnAddHearingAndPastHearingDatesIsEnabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate,
                "hearingOption","NEW_HEARING"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotThrowErrorsWhenPastHearingDateEnteredOnFirstHearingAndPastHearingDatesIsEnabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldThrowOnlyInvalidTimeErrorsWhenPastHearingDatesEnteredOnAddHearingAndPastHearingDatesIsEnabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        LocalDateTime dateWithInvalidTime = LocalDateTime.of(1990, 10, 2, 00, 00);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", dateWithInvalidTime,
                "hearingOption","NEW_HEARING"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "Enter a valid end time");
    }

    @Test
    void shouldPopulateConfirmationHearingFieldsWhenHearingDateInPastAndPastHearingDatesIsEnabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate,
                "hearingOption","NEW_HEARING"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        Map<String, Object> responseData = callbackResponse.getData();
        assertThat(responseData.get("showConfirmPastHearingDatesPage").equals("Yes"));
        assertThat(responseData.get("startDateFlag").equals("Yes"));
        assertThat(responseData.get("endDateFlag").equals("Yes"));
    }

    @Test
    void shouldNotPopulateConfirmationHearingFieldsWhenHearingDateInPastAndPastHearingDatesIsDisabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(false);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate,
                "hearingOption","NEW_HEARING"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData).doesNotContainKeys("showConfirmPastHearingDatesPage", "startDateFlag", "endDateFlag");
    }

    @Test
    void shouldNotPopulateConfirmationHearingFieldsWhenHearingDateInFutureAndPastHearingDatesIsEnabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", futureDate,
                "hearingEndDate", futureDate,
                "hearingOption","NEW_HEARING"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData).doesNotContainKeys("showConfirmPastHearingDatesPage", "startDateFlag", "endDateFlag");
    }

    @Test
    void shouldNotThrowErrorsWhenValidHearingDatesEntered() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", futureDate,
                "hearingEndDate", futureDate))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }
}
