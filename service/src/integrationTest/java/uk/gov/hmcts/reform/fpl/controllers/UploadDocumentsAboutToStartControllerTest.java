package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
public class UploadDocumentsAboutToStartControllerTest extends AbstractControllerTest {

    UploadDocumentsAboutToStartControllerTest() {
        super("upload-documents");
    }

    @Test
    void shouldRemoveShowCreatedByAndDateTimeUploadedFlag() {
        CaseDetails caseDetails = callbackRequest().getCaseDetails();

        caseDetails.getData().put("showCreatedByAndDateTimeUploadedFlag", YES);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertNull(callbackResponse.getData().get("showCreatedByAndDateTimeUploadedFlag"));
    }
}
