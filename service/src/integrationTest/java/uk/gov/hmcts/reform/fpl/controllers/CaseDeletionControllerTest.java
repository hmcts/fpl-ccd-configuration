package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@WebMvcTest(CaseDeletionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseDeletionControllerTest extends AbstractCallbackTest {

    CaseDeletionControllerTest() {
        super("case-deletion");
    }

    @Test
    void shouldRemoveAllCaseDetailsWhenCalled() {
        CaseDetails caseDetails = callbackRequest().getCaseDetails();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData()).isEmpty();
    }
}
