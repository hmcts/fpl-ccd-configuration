package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(UploadDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadDocumentsAboutToStartControllerTest extends AbstractCallbackTest {

    @MockBean
    private ManageDocumentService manageDocumentService;

    UploadDocumentsAboutToStartControllerTest() {
        super("upload-documents");
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldPrepareAllowMarkDocumentConfidential(boolean allow) {
        when(manageDocumentService.allowMarkDocumentConfidential(any())).thenReturn(allow);

        CaseDetails caseDetails = CaseDetails.builder().data(
            Map.of()).build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(callbackRequest);

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        List<Element<ApplicationDocument>> actualDocument = responseCaseData.getTemporaryApplicationDocuments();

        assertThat(actualDocument).hasSize(1)
            .allMatch(c -> (allow ? "YES" : "NO").equals(c.getValue().getAllowMarkDocumentConfidential()));
    }
}
