package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.SecureDocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsAboutToSubmitControllerTest extends AbstractCallbackTest {
    private static final String USER_NAME = "HMCTS";
    private static final Long CASE_ID = 12345L;
    private static final DocumentReference document = testDocumentReference();

    @Autowired
    private Time time;

    UploadC2DocumentsAboutToSubmitControllerTest() {
        super("upload-c2");
    }

    @BeforeEach
    void before() {
        givenCurrentUserWithName(USER_NAME);
        givenCurrentUser(createUserDetailsWithHmctsRole());
    }

    @Test
    void shouldCreateC2DocumentBundle() {
        Map<String, Object> data = createTemporaryC2Document();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(createCase(data));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        C2DocumentBundle uploadedC2DocumentBundle = caseData.getC2DocumentBundle().get(0).getValue();

        String expectedDateTime = formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME);

        assertThat(uploadedC2DocumentBundle.getUploadedDateTime()).isEqualTo(expectedDateTime);
        assertThat(caseData.getUploadAdditionalApplicationsEventData().getTemporaryC2Document()).isNull();
        assertThat(caseData.getC2DocumentBundle()).hasSize(1);
        assertC2BundleDocument(uploadedC2DocumentBundle, "Test description");
        assertSupportingEvidenceBundle(uploadedC2DocumentBundle);
        assertThat(uploadedC2DocumentBundle.getAuthor()).isEqualTo(USER_NAME);
    }

    @Test
    void shouldAppendAnAdditionalC2DocumentBundleWhenAC2DocumentBundleIsPresent() {
        CaseDetails caseDetails = callbackRequest().getCaseDetails();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        C2DocumentBundle existingC2Document = caseData.getC2DocumentBundle().get(0).getValue();

        C2DocumentBundle appendedC2Document = caseData.getC2DocumentBundle().get(1).getValue();

        String expectedDateTime = formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME);

        assertThat(appendedC2Document.getUploadedDateTime()).isEqualTo(expectedDateTime);
        assertC2BundleDocument(existingC2Document, "C2 document one");
        assertC2BundleDocument(appendedC2Document, "C2 document two");
        assertThat(caseData.getUploadAdditionalApplicationsEventData().getTemporaryC2Document()).isNull();
        assertThat(caseData.getC2DocumentBundle()).hasSize(2);
        assertThat(appendedC2Document.getAuthor()).isEqualTo(USER_NAME);
    }

    private void assertC2BundleDocument(C2DocumentBundle documentBundle, String description) {
        Document document = document();

        assertThat(documentBundle.getDocument().getUrl()).isEqualTo(document.links.self.href);
        assertThat(documentBundle.getDocument().getFilename()).isEqualTo(document.originalDocumentName);
        assertThat(documentBundle.getDocument().getBinaryUrl()).isEqualTo(document.links.binary.href);
        assertThat(documentBundle.getDescription()).isEqualTo(description);
    }

    private void assertSupportingEvidenceBundle(C2DocumentBundle documentBundle) {
        List<SupportingEvidenceBundle> supportingEvidenceBundle =
            unwrapElements(documentBundle.getSupportingEvidenceBundle());

        assertThat(supportingEvidenceBundle).first()
            .extracting(
                SupportingEvidenceBundle::getName,
                SupportingEvidenceBundle::getNotes,
                SupportingEvidenceBundle::getDateTimeReceived,
                SupportingEvidenceBundle::getDateTimeUploaded,
                SupportingEvidenceBundle::getDocument,
                SupportingEvidenceBundle::getUploadedBy
            ).containsExactly(
            "Supporting document",
            "Document notes",
            time.now().minusDays(1),
            time.now(),
            document,
            USER_NAME
        );
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
            .dateTimeReceived(time.now().minusDays(1))
            .dateTimeUploaded(time.now())
            .document(document)
            .build();
    }

    private UserDetails createUserDetailsWithHmctsRole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Hudson")
            .forename("Steve")
            .email("steve.hudson@gov.uk")
            .roles(Arrays.asList("caseworker-publiclaw-courtadmin", "caseworker-publiclaw-judiciary"))
            .build();
    }
}
