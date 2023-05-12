package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsControllerSubmittedTest extends ManageDocumentsControllerSubmittedBaseTest {

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    ManageDocumentsControllerSubmittedTest() {
        super("manage-documents");
    }

    @BeforeEach
    void init() {
        given(coreCaseDataService.performPostSubmitCallbackWithoutChange(any(), eq("internal-change-manage-doc")))
            .willReturn(CaseDetails.builder().build());
    }

    @Test
    void shouldTriggerPostSubmitCallback() {
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments("test",
            false));
    }
}
