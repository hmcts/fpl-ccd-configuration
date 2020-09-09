package uk.gov.hmcts.reform.fpl.controllers;

import io.jsonwebtoken.lang.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsAboutToSubmitControllerTest extends AbstractControllerTest {
    private static final String USER_NAME = "Emma Taylor";
    private static final Long CASE_ID = 12345L;

    @MockBean
    private IdamClient idamClient;

    UploadC2DocumentsAboutToSubmitControllerTest() {
        super("upload-c2");
    }

    @BeforeEach
    void before() {
        given(idamClient.getUserInfo(USER_AUTH_TOKEN)).willReturn(UserInfo.builder().name("Emma Taylor").build());
    }

    @Test
    void shouldCreateC2DocumentBundle() {
        Map<String, Object> data = createTemporaryC2Document();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(createCase(data));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        C2DocumentBundle uploadedC2DocumentBundle = caseData.getC2DocumentBundle().get(0).getValue();

        String expectedDateTime = formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME);

        assertThat(uploadedC2DocumentBundle.getUploadedDateTime()).isEqualTo(expectedDateTime);
        assertThat(caseData.getTemporaryC2Document()).isNull();
        assertThat(caseData.getC2DocumentBundle()).hasSize(1);
        assertC2BundleDocument(uploadedC2DocumentBundle, "Test description");
        assertThat(uploadedC2DocumentBundle.getAuthor()).isEqualTo(USER_NAME);
    }

    @Test
    void shouldAppendAnAdditionalC2DocumentBundleWhenAC2DocumentBundleIsPresent() {
        CaseDetails caseDetails = callbackRequest().getCaseDetails();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        C2DocumentBundle existingC2Document = caseData.getC2DocumentBundle().get(0).getValue();
        C2DocumentBundle updatedExistingC2Document = existingC2Document.toBuilder()
            .supportingEvidenceBundle(wrapElements(createSupportingEvidenceBundle()))
            .build();

        C2DocumentBundle appendedC2Document = caseData.getC2DocumentBundle().get(1).getValue();
        C2DocumentBundle updatedAppendedC2Document = appendedC2Document.toBuilder()
            .supportingEvidenceBundle(wrapElements(createSupportingEvidenceBundle()))
            .build();

        String expectedDateTime = formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME);

        assertThat(updatedAppendedC2Document.getUploadedDateTime()).isEqualTo(expectedDateTime);
        assertC2BundleDocument(updatedExistingC2Document, "C2 document one");
        assertC2BundleDocument(updatedAppendedC2Document, "C2 document two");
        assertThat(caseData.getTemporaryC2Document()).isNull();
        assertThat(caseData.getC2DocumentBundle()).hasSize(2);
        assertThat(updatedAppendedC2Document.getAuthor()).isEqualTo(USER_NAME);
    }

    private void assertC2BundleDocument(C2DocumentBundle documentBundle, String description) {
        Document document = document();

        assertThat(documentBundle.getDocument().getUrl()).isEqualTo(document.links.self.href);
        assertThat(documentBundle.getDocument().getFilename()).isEqualTo(document.originalDocumentName);
        assertThat(documentBundle.getDocument().getBinaryUrl()).isEqualTo(document.links.binary.href);
        assertThat(documentBundle.getDescription()).isEqualTo(description);

        List<SupportingEvidenceBundle> supportingBundle = unwrapElements(documentBundle.getSupportingEvidenceBundle());

        if(!Collections.isEmpty(supportingBundle)) {
            SupportingEvidenceBundle supportingEvidenceBundle = supportingBundle.get(0);
            assertThat(supportingEvidenceBundle.getName()).isEqualTo("Supporting document");
            assertThat(supportingEvidenceBundle.getNotes()).isEqualTo("Document notes");
        }
    }

    private Map<String, Object> createTemporaryC2Document() {
        return Map.of(
            "c2ApplicationType", Map.of(
                "type", "WITH_NOTICE"),
            "temporaryC2Document", Map.of(
                "document", Map.of(
                    "document_url", "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4",
                    "document_binary_url",
                    "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary",
                    "document_filename", "file.pdf"),
                "description", "Test description",
                "supportingEvidenceBundle", wrapElements(createSupportingEvidenceBundle())
            )
        );
    }

    private CaseDetails createCase(Map<String, Object> data) {
        return CaseDetails.builder()
            .data(data)
            .id(CASE_ID)
            .build();
    }

    private SupportingEvidenceBundle createSupportingEvidenceBundle() {
        return SupportingEvidenceBundle.builder()
            .name("Supporting document")
            .notes("Document notes")
            .dateTimeReceived(LocalDateTime.now().minusDays(1))
            .dateTimeUploaded(LocalDateTime.now())
            .document(TestDataHelper.testDocumentReference())
            .build();
    }
}
