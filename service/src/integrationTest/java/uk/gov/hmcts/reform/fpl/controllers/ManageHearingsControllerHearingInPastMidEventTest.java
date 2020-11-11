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


    ManageHearingsControllerHearingInPastMidEventTest() {
        super("manage-hearings");
    }

    @Test
    void shouldPopulateHearingDatesToCorrectOneWhenIncorrectAndToggledOn() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        LocalDateTime correctStartDate = LocalDateTime.of(2050, 6, 5, 11, 0);
        LocalDateTime correctEndDate = LocalDateTime.of(2050, 6, 5, 11, 0);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", LocalDateTime.of(2001, 6, 5, 11, 0),
                "hearingEndDate", LocalDateTime.of(2001, 6, 5, 11, 0),
                "hearingStartDateConfirmation", correctStartDate,
                "hearingEndDateConfirmation", correctEndDate,
                "hearingDateConfirmation","No"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData.get("hearingStartDate").equals(correctStartDate));
        assertThat(responseData.get("hearingEndDate").equals(correctEndDate));
    }

    @Test
    void shouldPopulateHearingEndDateToCorrectOneWhenIncorrectAndToggledOn() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        LocalDateTime correctEndDate = LocalDateTime.of(2050, 6, 5, 11, 0);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", LocalDateTime.of(2001, 6, 5, 11, 0),
                "hearingEndDate", LocalDateTime.of(2001, 6, 5, 11, 0),
                "hearingEndDateConfirmation", correctEndDate,
                "hearingDateConfirmation","No"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData.get("hearingStartDate").equals(LocalDateTime.of(2001, 6, 5, 11, 0)));
        assertThat(responseData.get("hearingEndDate").equals(correctEndDate));
    }

    @Test
    void shouldPopulateHearingStartDateToCorrectOneWhenIncorrectAndToggledOn() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        LocalDateTime correctStartDate = LocalDateTime.of(2050, 6, 5, 11, 0);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", LocalDateTime.of(2001, 6, 5, 11, 0),
                "hearingEndDate", LocalDateTime.of(2001, 6, 5, 11, 0),
                "hearingStartDateConfirmation", correctStartDate,
                "hearingDateConfirmation","No"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData.get("hearingStartDate").equals(correctStartDate));
        assertThat(responseData.get("hearingEndDate").equals(LocalDateTime.of(2001, 6, 5, 11, 0)));
    }

    @Test
    void shouldNotChangeHearingDateIfCorrectAndToggledOn() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", LocalDateTime.of(2001, 6, 5, 11, 0),
                "hearingStartDateConfirmation", LocalDateTime.of(2050, 6, 5, 11, 0),
                "hearingDateConfirmation","Yes"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData.get("hearingStartDate").equals(LocalDateTime.of(2001, 6, 5, 11, 0)));
    }

    @Test
    void shouldNotChangeHearingDateIfToggledOff() {
        given(featureToggleService.isAddHearingsInPastEnabled()).willReturn(false);

        CaseDetails caseDetails = CaseDetails.builder().data(Map.of()).build();

        postMidEvent(caseDetails, "hearing-in-past");

        verify(hearingsService, never()).changeHearingDateToDateAddedOnConfirmationPage(any());
    }
}
