package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ManageHearingsService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ActiveProfiles("integration-test")
@OverrideAutoConfiguration(enabled = true)
@WebMvcTest(ManageHearingsController.class)
public class ManageHearingsControllerHearingInPastMidEventTest extends AbstractControllerTest {

    private static final String CASE_ID = "12345";

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private ManageHearingsService hearingsService;

    @Autowired
    private Time time;

    private LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

    ManageHearingsControllerHearingInPastMidEventTest() {
        super("manage-hearings");
    }

    @Test
    void shouldPopulateHearingDatesToCorrectOneWhenIncorrectAndToggledOn() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        LocalDateTime correctStartDate = LocalDateTime.now().minusDays(2);
        LocalDateTime correctEndDate = LocalDateTime.now().minusDays(2);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate,
                "hearingStartDateConfirmation", correctStartDate,
                "hearingEndDateConfirmation", correctEndDate,
                "confirmHearingDate","No"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData.get("hearingStartDate").equals(correctStartDate));
        assertThat(responseData.get("hearingEndDate").equals(correctEndDate));
    }

    @Test
    void shouldCorrectHearingDatesWhenIncorrectAndPastHearingDatesIsEnabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        LocalDateTime correctEndDate = LocalDateTime.now().minusDays(2);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate,
                "hearingEndDateConfirmation", correctEndDate,
                "confirmHearingDate","No"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData.get("hearingStartDate").equals(pastDate));
        assertThat(responseData.get("hearingEndDate").equals(correctEndDate));
    }

    @Test
    void shouldPopulateHearingStartDateToCorrectOneWhenIncorrectAndToggledOn() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        LocalDateTime correctStartDate = LocalDateTime.now().plusDays(2);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate,
                "hearingStartDateConfirmation", correctStartDate,
                "confirmHearingDate","No"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData.get("hearingStartDate").equals(correctStartDate));
        assertThat(responseData.get("hearingEndDate").equals(pastDate));
    }

    @Test
    void shouldNotChangeHearingDateIfCorrectAndToggledOn() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "confirmHearingDate","Yes"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData.get("hearingStartDate").equals(pastDate));
    }

    @Test
    void shouldNotCorrectHearingDatesWhenPastHearingDatesIsDisabled() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(false);

        CaseDetails caseDetails = CaseDetails.builder().data(Map.of()).build();

        postMidEvent(caseDetails, "hearing-in-past");

        verify(hearingsService, never()).changeHearingDateToDateAddedOnConfirmationPage(any());
    }
}
