package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Map;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@OverrideAutoConfiguration(enabled = true)
@WebMvcTest(ManageHearingsController.class)
public class ManageHearingsControllerValidateHearingDatesMidEventTest extends AbstractControllerTest {

    private static final String CASE_ID = "12345";

    @Autowired
    private Time time;

    ManageHearingsControllerValidateHearingDatesMidEventTest() {
        super("manage-hearings");
    }

    @Test
    void shouldThrowErrorsWhenInvalidHearingDatesEntered() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", time.now().minusDays(1),
                "hearingEndDate", time.now().minusDays(2)))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "validate-hearing-dates");

        assertThat(callbackResponse.getErrors()).containsExactly(
            "Enter a start date in the future",
            "Enter an end date in the future");
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
