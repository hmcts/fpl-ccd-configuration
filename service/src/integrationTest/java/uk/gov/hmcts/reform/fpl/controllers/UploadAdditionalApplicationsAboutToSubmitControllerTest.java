package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupplementsBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadAdditionalApplicationsAboutToSubmitControllerTest extends AbstractControllerTest {
    private static final String USER_NAME = "HMCTS";
    private static final Long CASE_ID = 12345L;
    private static final DocumentReference document = testDocumentReference();

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private RequestData requestData;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private Time time;

    UploadAdditionalApplicationsAboutToSubmitControllerTest() {
        super("upload-additional-applications");
    }

    @BeforeEach
    void before() {
        given(idamClient.getUserInfo(USER_AUTH_TOKEN)).willReturn(UserInfo.builder().name(USER_NAME).build());
        given(idamClient.getUserDetails(eq(USER_AUTH_TOKEN))).willReturn(createUserDetailsWithHmctsRole());
        given(requestData.authorisation()).willReturn(USER_AUTH_TOKEN);
    }

    @Test
    void shouldCreateC2DocumentBundleWithoutSupplementsIncludedWhenAdditionalApplicationsToggledOff() {
        given(featureToggleService.isUploadAdditionalApplicationsEnabled()).willReturn(false);
        Map<String, Object> data = createTemporaryC2Document();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(createCase(data));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        C2DocumentBundle uploadedC2DocumentBundle = caseData.getC2DocumentBundle().get(0).getValue();

        String expectedDateTime = formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME);

        assertThat(uploadedC2DocumentBundle.getUploadedDateTime()).isEqualTo(expectedDateTime);
        assertThat(caseData.getTemporaryC2Document()).isNull();
        assertThat(caseData.getC2DocumentBundle()).hasSize(1);
        assertC2BundleDocument(uploadedC2DocumentBundle, "Test description");
        assertSupportingEvidenceBundle(uploadedC2DocumentBundle);
        assertThat(uploadedC2DocumentBundle.getAuthor()).isEqualTo(USER_NAME);
    }

    @Test
    void shouldCreateC2DocumentBundleWithSupplementsIncludedWhenAdditionalApplicationsToggledOn() {
        given(featureToggleService.isUploadAdditionalApplicationsEnabled()).willReturn(true);
        Map<String, Object> data = createTemporaryC2Document();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(createCase(data));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        C2DocumentBundle uploadedC2DocumentBundle = caseData.getC2DocumentBundle().get(0).getValue();

        String expectedDateTime = formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME);

        assertThat(uploadedC2DocumentBundle.getUploadedDateTime()).isEqualTo(expectedDateTime);
        assertThat(caseData.getTemporaryC2Document()).isNull();
        assertThat(caseData.getC2DocumentBundle()).hasSize(1);
        assertC2BundleDocument(uploadedC2DocumentBundle, "Test description");
        assertSupportingEvidenceBundle(uploadedC2DocumentBundle);
        assertSupplementsBundle(uploadedC2DocumentBundle);
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
        assertThat(caseData.getTemporaryC2Document()).isNull();
        assertThat(caseData.getC2DocumentBundle()).hasSize(2);
        assertThat(appendedC2Document.getAuthor()).isEqualTo(USER_NAME);
    }

    @Test
    void shouldRemoveTransientFieldsWhenNoLongerNeeded() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("temporaryC2Document", createTemporaryC2Document(),
            "c2ApplicationType", Map.of("type", WITHOUT_NOTICE),
                "additionalApplicationType", "C2_ORDER",
                "usePbaPayment", "Yes",
                "amountToPay", "Yes",
                "pbaNumber", "1234567",
                "clientCode", "123",
                "fileReference", "456"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getTemporaryC2Document()).isNull();
        assertThat(callbackResponse.getData().get("c2ApplicationType")).isNull();
        assertThat(caseData.getC2ApplicationType()).isNull();
        assertThat(caseData.getUsePbaPayment()).isNull();
        assertThat(caseData.getAmountToPay()).isNull();
        assertThat(caseData.getPbaNumber()).isNull();
        assertThat(caseData.getClientCode()).isNull();
        assertThat(caseData.getFileReference()).isNull();
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

        System.out.println("SUpporting is" + supportingEvidenceBundle);

        assertThat(supportingEvidenceBundle).first()
            .extracting(
                SupportingEvidenceBundle::getName,
                SupportingEvidenceBundle::getNotes,
                SupportingEvidenceBundle::getDateTimeUploaded,
                SupportingEvidenceBundle::getDocument,
                SupportingEvidenceBundle::getUploadedBy
            ).containsExactly(
            "Supporting document",
            "Document notes",
            time.now(),
            document,
            USER_NAME
        );
    }

    private void assertSupplementsBundle(C2DocumentBundle documentBundle) {
        List<SupplementsBundle> supplementsBundle =
            unwrapElements(documentBundle.getSupplementsBundle());

        System.out.println("SUp is" + supplementsBundle);

        assertThat(supplementsBundle).first()
            .extracting(
                SupplementsBundle::getName,
                SupplementsBundle::getNotes,
                SupplementsBundle::getDateTimeUploaded,
                SupplementsBundle::getDocument,
                SupplementsBundle::getUploadedBy
            ).containsExactly(
            "C13A - special guardianship order",
            "Supplement notes",
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
                "supportingEvidenceBundle", wrapElements(createSupportingEvidenceBundle()),
                "supplementsBundle", wrapElements(createSupplementsBundle())
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
            .dateTimeUploaded(time.now())
            .document(document)
            .build();
    }

    private SupplementsBundle createSupplementsBundle() {
        return SupplementsBundle.builder()
            .name("C13A - special guardianship order")
            .notes("Supplement notes")
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
