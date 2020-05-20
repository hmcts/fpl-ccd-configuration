package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class DraftOrdersControllerAboutToStartTest extends AbstractControllerTest {

    DraftOrdersControllerAboutToStartTest() {
        super("draft-standard-directions");
    }

    @Test
    void shouldPlaceDateOfIssueValue() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData().get("dateOfIssue")).isEqualTo(dateNow().toString());
    }
}
