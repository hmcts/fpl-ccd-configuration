package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;

@WebMvcTest(ChangeStateController.class)
@OverrideAutoConfiguration(enabled = true)
class ChangeStateControllerAboutToStartTest extends AbstractCallbackTest {

    ChangeStateControllerAboutToStartTest() {
        super("change-state");
    }

    @Test
    void shouldReturnMessageRelatedToCaseManagementWhenStateIsFinalHearing() {
        CaseData caseData = CaseData.builder().state(FINAL_HEARING).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        String expectedMessage = "Do you want to change the case state to case management?";

        assertThat(response.getData()).extracting("nextStateLabelContent").isEqualTo(expectedMessage);
    }

    @Test
    void shouldNotInitialiseMessageWhenStateIsClosed() {
        CaseData caseData = CaseData.builder().state(CLOSED).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getData()).doesNotContainKey("nextStateLabelContent");
    }
}
