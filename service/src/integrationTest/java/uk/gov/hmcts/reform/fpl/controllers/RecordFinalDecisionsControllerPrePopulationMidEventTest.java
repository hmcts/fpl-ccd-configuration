package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(RecordFinalDecisionsController.class)
@OverrideAutoConfiguration(enabled = true)
class RecordFinalDecisionsControllerPrePopulationMidEventTest extends AbstractCallbackTest {

    RecordFinalDecisionsControllerPrePopulationMidEventTest() {
        super("record-final-decisions");
    }

    @Test
    void shouldPrePopulateChildrenFinalDecisions() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(
                Child.builder().party(
                    ChildParty.builder()
                        .firstName("John")
                        .lastName("Smith")
                        .build())
                    .build()
                )).build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "pre-populate");

        assertThat(response.getData()).containsKey("childFinalDecisionDetails00");
    }

    @Test
    void shouldReturnAnErrorIfNoChildrenSelected() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren(NO.getValue())
            .childSelector(Selector.builder().selected(emptyList()).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "pre-populate");

        assertThat(response.getErrors()).containsOnly("Select the children with a final order or other decision");
    }

    @Test
    void shouldNotReturnAnErrorWhenChildrenSelected() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren(NO.getValue())
            .childSelector(Selector.builder().selected(List.of(1,2,3)).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "pre-populate");

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorWhenAppliesToAllChildren() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren(YES.getValue())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "pre-populate");

        assertThat(response.getErrors()).isEmpty();
    }

}
