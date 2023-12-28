package uk.gov.hmcts.reform.fpl.controllers;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.Map;

@WebMvcTest(ManageDocumentService.class)
@OverrideAutoConfiguration(enabled = true)
class EnterC1WithSupplementControllerAboutToSubmitTest extends AbstractCallbackTest {

    EnterC1WithSupplementControllerAboutToSubmitTest() {
        super("enter-c1-with-supplement");
    }

    @Test
    void shouldRemoveSubmittedC1WithSupplementWhenClearFlagIsYes() {
        Map<String, Object> caseDetails = postAboutToSubmitEvent(CaseData.builder()
            .id(10L)
            .state(State.OPEN)
            .caseName("Updated CaseName")
            .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                .clearSubmittedC1WithSupplement("yes")
                .build())
            .build()).getData();

        Assertions.assertThat(caseDetails.get("submittedC1WithSupplement")).isNull();
    }
}
