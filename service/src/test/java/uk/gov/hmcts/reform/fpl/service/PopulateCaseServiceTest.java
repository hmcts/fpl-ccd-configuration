package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FixedTimeConfiguration.class, JacksonAutoConfiguration.class,
    PopulateCaseService.class})
class PopulateCaseServiceTest {

    private static final Document MOCK_DOCUMENT = document();
    private static final Document MOCK_SDO_DOCUMENT = document();
    private static final Document MOCK_APPLICATION_DOCUMENT = document();

    @Autowired
    private Time time;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Autowired
    private PopulateCaseService service;

    @BeforeAll()
    static void setupAll() {
        MOCK_DOCUMENT.originalDocumentName = "mockFile.txt";
        MOCK_SDO_DOCUMENT.originalDocumentName = "mockSDO.pdf";
        MOCK_APPLICATION_DOCUMENT.originalDocumentName = "mockSubmittedApplication.pdf";
    }

    @BeforeEach
    void setup() {
        given(uploadDocumentService.uploadPDF(new byte[] {}, "mockFile.txt")).willReturn(MOCK_DOCUMENT);
        given(uploadDocumentService.uploadPDF(new byte[] {}, "mockSDO.pdf")).willReturn(MOCK_SDO_DOCUMENT);
        given(uploadDocumentService.uploadPDF(new byte[] {}, "mockSubmittedApplication.pdf"))
            .willReturn(MOCK_APPLICATION_DOCUMENT);
    }

    @Test
    void shouldReturnTimeBasedAndDocumentData() {
        var expectedMockDocument = Map.of("documentStatus",
            "Attached",
            "typeOfDocument",
            DocumentReference.builder()
                .filename(MOCK_DOCUMENT.originalDocumentName)
                .url("fakeUrl")
                .binaryUrl("fakeBinaryUrl")
                .build());
        var expectedSubmittedForm = DocumentReference.builder()
            .filename(MOCK_APPLICATION_DOCUMENT.originalDocumentName)
            .url("fakeUrl")
            .binaryUrl("fakeBinaryUrl")
            .build();

        Map<String, Object> data = service.getTimeBasedAndDocumentData();

        assertThat(data).extracting("dateAndTimeSubmitted").isEqualTo(time.now().toString());
        assertThat(data).extracting("dateSubmitted").isEqualTo(time.now().toLocalDate().toString());
        assertThat(data).extracting("submittedForm").isEqualTo(expectedSubmittedForm);
        assertThat(data).extracting("documents_checklist_document").isEqualTo(expectedMockDocument);
        assertThat(data).extracting("documents_threshold_document").isEqualTo(expectedMockDocument);
        assertThat(data).extracting("documents_socialWorkCarePlan_document").isEqualTo(expectedMockDocument);
        assertThat(data).extracting("documents_socialWorkAssessment_document").isEqualTo(expectedMockDocument);
        assertThat(data).extracting("documents_socialWorkEvidenceTemplate_document").isEqualTo(expectedMockDocument);

        //verify(uploadDocumentService).uploadPDF(new byte[]{}, "mockFile.txt");
        //verify(uploadDocumentService).uploadPDF(new byte[]{}, "mockSubmittedApplication.pdf");
    }

    @Test
    void shouldReturnUpdatedSDODataWithoutMutatingOriginalMap() {
        Map<String, Object> originalMap = Map.of("standardDirectionOrder", Map.of("orderDoc", "initialValue"));

        var updatedSDOData = service.getUpdatedSDOData(originalMap);

        assertThat(updatedSDOData).extracting("orderDoc")
            .isEqualTo(DocumentReference.builder()
                .filename(MOCK_SDO_DOCUMENT.originalDocumentName)
                .url("fakeUrl")
                .binaryUrl("fakeBinaryUrl")
                .build());
        assertThat(originalMap).extracting("standardDirectionOrder").extracting("orderDoc").isEqualTo("initialValue");
        //verify(uploadDocumentService).uploadPDF(new byte[]{}, "mockSDO.pdf");
    }
}
