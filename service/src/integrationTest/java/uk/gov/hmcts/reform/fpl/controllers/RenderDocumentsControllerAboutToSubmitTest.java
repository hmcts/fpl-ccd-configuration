package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@WebMvcTest(UploadDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class RenderDocumentsControllerAboutToSubmitTest extends AbstractCallbackTest {

    @MockBean
    private IdentityService identityService;

    @MockBean
    private DocumentUploadHelper documentUploadHelper;

    RenderDocumentsControllerAboutToSubmitTest() {
        super("render-documents");
    }

    @Test
    void shouldNotRenderIfDocumentsAreUpdatedInTheCase() {
        when(identityService.generateId()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID());
        given(documentUploadHelper.getUploadedDocumentUserDetails()).willReturn("siva@swansea.gov.uk");

        CaseDetails caseDetails = CaseDetails.builder().data(someCaseDataWithDocuments()).build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);

        assertThat((String) callbackResponse.getData().get("documentViewLA")).isNull();
        assertThat((String) callbackResponse.getData().get("documentViewHMCTS")).isNull();
        assertThat((String) callbackResponse.getData().get("documentViewNC")).isNull();
        assertThat(callbackResponse.getData().get("showFurtherEvidenceTab")).isNull();
    }

    @Test
    void shouldNotRenderIfNoDocuments() {
        when(identityService.generateId()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID());
        given(documentUploadHelper.getUploadedDocumentUserDetails()).willReturn("siva@swansea.gov.uk");

        CaseDetails caseDetails = CaseDetails.builder().data(Map.of()).build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);

        assertThat((String) callbackResponse.getData().get("documentViewLA")).isNull();
        assertThat((String) callbackResponse.getData().get("documentViewHMCTS")).isNull();
        assertThat((String) callbackResponse.getData().get("documentViewNC")).isNull();
        assertThat(callbackResponse.getData().get("showFurtherEvidenceTab")).isNull();
    }

    private Map<String, Object> someCaseDataWithDocuments() {
        return Map.of(
            "applicationDocuments", List.of(
                Map.of(
                    "id", UUID.randomUUID(),
                    "value", Map.of(
                        "document", Map.of(
                            "document_url", "https://AnotherdocuURL",
                            "document_filename", "mockChecklist.pdf",
                            "document_binary_url", "http://dm-store:8080/documents/fakeUrl/binary"
                        ),
                        "documentType", "SOCIAL_WORK_STATEMENT"
                    )
                )
            )
        );
    }
}
