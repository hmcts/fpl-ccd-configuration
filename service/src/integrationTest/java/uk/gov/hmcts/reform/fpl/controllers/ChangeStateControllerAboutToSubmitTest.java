package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CloseCase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@WebMvcTest(ChangeStateController.class)
@OverrideAutoConfiguration(enabled = true)
class ChangeStateControllerAboutToSubmitTest extends AbstractCallbackTest {

    ChangeStateControllerAboutToSubmitTest() {
        super("change-state");
    }

    @Test
    void shouldChangeStateToCaseManagementWhenCurrentStateIsFinalHearingAndYesIsSelected() {
        CaseData caseData = caseData(FINAL_HEARING, YES.getValue());

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        assertThat(response.getData()).extracting("state").isEqualTo(CASE_MANAGEMENT.getValue());
    }

    @Test
    void shouldMigrateStateFromClosedToFinalHearing() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .closedStateRadioList(FINAL_HEARING)
            .closeCaseTabField(CloseCase.builder()
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        assertThat(response.getData())
            .extracting("state", "deprivationOfLiberty", "closeCaseTabField")
            .containsExactly(FINAL_HEARING.getValue(), null, null);
    }

    @Test
    void shouldNotChangeStateWhenNoIsSelected() {
        CaseData caseData = caseData(FINAL_HEARING, NO.getValue());

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        assertThat(response.getData()).extracting("state").isEqualTo(FINAL_HEARING.getValue());
    }

    private CaseData caseData(State state, String changeState) {
        return CaseData.builder()
            .state(state)
            .confirmChangeState(changeState)
            .build();
    }
}
