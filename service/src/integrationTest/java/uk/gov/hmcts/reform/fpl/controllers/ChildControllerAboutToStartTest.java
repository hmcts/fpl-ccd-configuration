package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerAboutToStartTest extends AbstractCallbackTest {

    ChildControllerAboutToStartTest() {
        super("enter-children");
    }

    @Test
    void aboutToStartShouldPrepopulateChildrenDataWhenNoChildExists() {
        CaseData caseData = CaseData.builder().build();
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);

        assertThat(callbackResponse.getData()).containsKey("children1");
    }
}
