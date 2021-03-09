package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_STATEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(UploadDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadDocumentsAboutToSubmitControllerTest extends AbstractCallbackTest {
    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();
    private static final String FILE_URL = "https://docuURL";
    private static final String ANOTHER_FILE_URL = "https://AnotherdocuURL";
    private static final String FILE_NAME = "mockChecklist.pdf";
    private static final String FILE_BINARY_URL = "http://dm-store:8080/documents/fakeUrl/binary";
    private static final String USER = "kurt@swansea.gov.uk";
    private static final String ANOTHER_USER = "siva@swansea.gov.uk";

    @MockBean
    private IdentityService identityService;

    @MockBean
    private DocumentUploadHelper documentUploadHelper;

    UploadDocumentsAboutToSubmitControllerTest() {
        super("upload-documents");
    }

    @Test
    void shouldUpdateApplicationDocumentsWhenExistingInCaseDetailsBefore() {
        when(identityService.generateId()).thenReturn(UUID_1).thenReturn(UUID_2);
        given(documentUploadHelper.getUploadedDocumentUserDetails()).willReturn(ANOTHER_USER);

        CaseDetails caseDetailsBefore = CaseDetails.builder().data(
            Map.of(
                "applicationDocuments", List.of(
                    Map.of(
                        "id", UUID_1,
                        "value", Map.of(
                            "document", Map.of(
                                "document_url", FILE_URL,
                                "document_filename", FILE_NAME,
                                "document_binary_url", FILE_BINARY_URL
                            ),
                            "uploadedBy", USER,
                            "dateTimeUploaded", "2020-12-03T02:03:04.000010",
                            "documentType", "SOCIAL_WORK_STATEMENT"
                        )
                    )
                )
            )).build();

        CaseDetails caseDetails = CaseDetails.builder().data(
            Map.of(
                "applicationDocuments", List.of(
                    Map.of(
                        "id", UUID_1,
                        "value", Map.of(
                            "document", Map.of(
                                "document_url", ANOTHER_FILE_URL,
                                "document_filename", FILE_NAME,
                                "document_binary_url", FILE_BINARY_URL
                            ),
                            "documentType", "SOCIAL_WORK_STATEMENT"
                        )
                    )
                )
            )).build();


        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        List<Element<ApplicationDocument>> actualDocument = responseCaseData.getApplicationDocuments();

        assertThat(actualDocument).isEqualTo(List.of(
            element(UUID_1, ApplicationDocument.builder()
                .documentType(SOCIAL_WORK_STATEMENT)
                .document(DocumentReference.builder()
                    .binaryUrl(FILE_BINARY_URL)
                    .filename(FILE_NAME)
                    .url(ANOTHER_FILE_URL)
                    .build())
                .uploadedBy(ANOTHER_USER)
                .dateTimeUploaded(now())
                .build()))
        );
    }
}
