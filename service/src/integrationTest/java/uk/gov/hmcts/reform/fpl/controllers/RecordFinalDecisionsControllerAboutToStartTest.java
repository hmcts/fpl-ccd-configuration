package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(RecordFinalDecisionsController.class)
@OverrideAutoConfiguration(enabled = true)
class RecordFinalDecisionsControllerAboutToStartTest extends AbstractCallbackTest {

    RecordFinalDecisionsControllerAboutToStartTest() {
        super("record-final-decisions");
    }

    @Test
    void shouldPrePpopulateFields() {
        CaseData caseData = CaseData.builder().build();
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getData()).containsKeys("childSelector", "children_label", "close_case_label");
    }

}
