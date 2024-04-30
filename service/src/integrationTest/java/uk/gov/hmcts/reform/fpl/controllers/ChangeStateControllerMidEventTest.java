package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;

@WebMvcTest(ChangeStateController.class)
@OverrideAutoConfiguration(enabled = true)
class ChangeStateControllerMidEventTest extends AbstractCallbackTest {
    ChangeStateControllerMidEventTest() {
        super("change-state");
    }

    @Test
    void shouldReturnErrorsWhenFinalOrdersOnChildrenHaveNotBeenRemovedOnAClosedCase() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .children1(ElementUtils.wrapElements(
                Child.builder()
                    .finalOrderIssued("Some data")
                    .finalOrderIssuedType("Some issue type")
                    .party(ChildParty.builder().build())
                    .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenFinalOrdersOnChildrenHaveNotBeenRemovedOnAFinalHearingCase() {
        CaseData caseData = CaseData.builder()
            .state(FINAL_HEARING)
            .children1(ElementUtils.wrapElements(
                Child.builder()
                    .finalOrderIssued("Some data")
                    .finalOrderIssuedType("Some issue type")
                    .party(ChildParty.builder().build())
                    .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenFinalOrdersOnChildrenHaveBeenRemoved() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .children1(ElementUtils.wrapElements(
                Child.builder()
                    .party(ChildParty.builder().build())
                    .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getErrors()).isEmpty();
    }
}
