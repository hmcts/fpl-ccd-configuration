package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;

@ActiveProfiles("integration-test")
@WebMvcTest(ChangeStateController.class)
@OverrideAutoConfiguration(enabled = true)
public class ChangeStateControllerAboutToStartTest extends AbstractControllerTest {

    ChangeStateControllerAboutToStartTest() {
        super("change-state");
    }

    @Test
    void shouldReturnMessageRelatedToCaseManagementWhenStateIsFinalHearing() {
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of()).state(FINAL_HEARING.getValue()).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        String expectedMessage = "Do you want to change the case state to case management?";

        assertThat(response.getData()).extracting("nextStateLabelContent").isEqualTo(expectedMessage);
    }

    @Test
    void shouldNotInitialiseMessageWhenStateIsClosed() {
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of()).state(CLOSED.getValue()).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getData()).extracting("nextStateLabelContent").isNull();
    }
}
