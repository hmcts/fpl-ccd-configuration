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
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.SupplementType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.JUDICIARY;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadAdditionalApplicationsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadAdditionalApplicationsAboutToSubmitControllerTest extends AbstractCallbackTest {

    private static final String USER_NAME = "HMCTS";
    private static final Long CASE_ID = 12345L;
    private static final String LOCAL_AUTHORITY_NAME = "Swansea local authority";
    private static final String APPLICANT_SOMEONE_ELSE = "SOMEONE_ELSE";
    private static final String APPLICANT = "applicant";
    private static final String OTHER_APPLICANT_NAME = "some other name";
    private static final String EXPECTED_LOCAL_AUTHORITY_NAME = "Swansea local authority, Applicant";

    private static final DocumentReference uploadedDocument = testDocumentReference();
    private static final DocumentReference sealedDocument = testDocumentReference();

    @MockBean
    private RequestData requestData;

    @MockBean
    private DocumentSealingService documentSealingService;

    @Autowired
    private Time time;

    UploadAdditionalApplicationsAboutToSubmitControllerTest() {
        super("upload-additional-applications");
    }

    @BeforeEach
    void before() {
        given(requestData.authorisation()).willReturn(USER_AUTH_TOKEN);
        given(idamClient.getUserDetails(eq(USER_AUTH_TOKEN))).willReturn(createUserDetailsWithHmctsRole());
        given(documentSealingService.sealDocument(uploadedDocument)).willReturn(sealedDocument);
    }

    @Test
    void shouldCreateAdditionalApplicationsBundleWithC2DocumentWhenC2OrderIsSelectedAndSupplementsIncluded() {
        Map<String, Object> data = new HashMap<>();
        data.put("caseLocalAuthorityName", LOCAL_AUTHORITY_NAME);
        data.put("additionalApplicationType", List.of("C2_ORDER"));
        data.putAll(createTemporaryC2Document());
        PBAPayment temporaryPbaPayment = createPbaPayment();
        data.put("temporaryPbaPayment", temporaryPbaPayment);
        data.put("applicantsList", APPLICANT);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(createCase(data));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        AdditionalApplicationsBundle additionalApplicationsBundle
            = caseData.getAdditionalApplicationsBundle().get(0).getValue();

        C2DocumentBundle uploadedC2DocumentBundle = additionalApplicationsBundle.getC2DocumentBundle();

        assertC2DocumentBundle(uploadedC2DocumentBundle);
        assertThat(uploadedC2DocumentBundle.getApplicantName()).isEqualTo(EXPECTED_LOCAL_AUTHORITY_NAME);
        assertThat(additionalApplicationsBundle.getPbaPayment()).isEqualTo(temporaryPbaPayment);
        assertTemporaryFieldsAreRemoved(caseData);
    }

    @Test
    void shouldCreateAdditionalApplicationsBundleWithOtherApplicationsBundleWhenOtherOrderIsSelected() {
        Map<String, Object> data = new HashMap<>();
        data.put("caseLocalAuthorityName", LOCAL_AUTHORITY_NAME);
        data.put("additionalApplicationType", List.of("OTHER_ORDER"));
        data.putAll(createTemporaryOtherApplicationDocument());
        PBAPayment temporaryPbaPayment = createPbaPayment();
        data.put("temporaryPbaPayment", temporaryPbaPayment);
        data.put("applicantsList", APPLICANT_SOMEONE_ELSE);
        data.put("otherApplicant", OTHER_APPLICANT_NAME);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(createCase(data));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        AdditionalApplicationsBundle additionalApplicationsBundle
            = caseData.getAdditionalApplicationsBundle().get(0).getValue();

        assertOtherApplicationsBundle(additionalApplicationsBundle.getOtherApplicationsBundle());
        assertThat(additionalApplicationsBundle.getOtherApplicationsBundle().getApplicantName())
            .isEqualTo(OTHER_APPLICANT_NAME);

        assertThat(additionalApplicationsBundle.getPbaPayment()).isEqualTo(temporaryPbaPayment);
        assertTemporaryFieldsAreRemoved(caseData);
    }

    @Test
    void shouldCreateAdditionalApplicationsBundleWhenC2OrderAndOtherOrderAreSelected() {
        PBAPayment temporaryPbaPayment = createPbaPayment();

        Map<String, Object> data = new HashMap<>();
        data.put("caseLocalAuthorityName", LOCAL_AUTHORITY_NAME);
        data.put("additionalApplicationType", List.of("C2_ORDER", "OTHER_ORDER"));
        data.putAll(createTemporaryC2Document());
        data.putAll(createTemporaryOtherApplicationDocument());
        data.put("temporaryPbaPayment", temporaryPbaPayment);
        data.put("applicantsList", APPLICANT);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(createCase(data));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        AdditionalApplicationsBundle additionalApplicationsBundle
            = caseData.getAdditionalApplicationsBundle().get(0).getValue();

        assertC2DocumentBundle(additionalApplicationsBundle.getC2DocumentBundle());
        assertOtherApplicationsBundle(additionalApplicationsBundle.getOtherApplicationsBundle());
        assertThat(additionalApplicationsBundle.getPbaPayment()).isEqualTo(temporaryPbaPayment);

        assertThat(additionalApplicationsBundle.getC2DocumentBundle().getApplicantName())
            .isEqualTo(EXPECTED_LOCAL_AUTHORITY_NAME);
        assertThat(additionalApplicationsBundle.getOtherApplicationsBundle().getApplicantName())
            .isEqualTo(EXPECTED_LOCAL_AUTHORITY_NAME);
        assertTemporaryFieldsAreRemoved(caseData);
    }

    @Test
    void shouldAppendAnAdditionalC2DocumentBundleWhenAdditionalDocumentsBundleIsPresent() {
        CaseDetails caseDetails = callbackRequest().getCaseDetails();
        caseDetails.getData().put("caseLocalAuthorityName", LOCAL_AUTHORITY_NAME);
        caseDetails.getData().put("applicantsList", APPLICANT);
        caseDetails.getData().put("temporaryC2Document", Map.of("document", uploadedDocument));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);
        CaseData returnedCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        AdditionalApplicationsBundle appendedApplicationsBundle
            = returnedCaseData.getAdditionalApplicationsBundle().get(0).getValue();
        AdditionalApplicationsBundle existingApplicationsBundle
            = returnedCaseData.getAdditionalApplicationsBundle().get(1).getValue();

        C2DocumentBundle existingC2Document = existingApplicationsBundle.getC2DocumentBundle();
        C2DocumentBundle appendedC2Document = appendedApplicationsBundle.getC2DocumentBundle();

        String expectedDateTime = formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME);
        assertThat(appendedC2Document.getUploadedDateTime()).isEqualTo(expectedDateTime);
        assertDocument(existingC2Document.getDocument(), buildFromDocument(document()));
        assertDocument(appendedC2Document.getDocument(), sealedDocument);

        assertThat(returnedCaseData.getTemporaryC2Document()).isNull();
        assertThat(appendedC2Document.getAuthor()).isEqualTo(USER_NAME);
    }

    @Test
    void shouldRemoveTransientFieldsWhenNoLongerNeeded() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseLocalAuthorityName", LOCAL_AUTHORITY_NAME,
                "temporaryC2Document", createTemporaryC2Document(),
                "c2Type", WITHOUT_NOTICE,
                "applicantsList", createApplicantsDynamicList(APPLICANT),
                "additionalApplicationType", List.of("C2_ORDER"),
                "temporaryPbaPayment", createPbaPayment(),
                "amountToPay", "Yes",
                "temporaryOtherApplicationsBundle", OtherApplicationsBundle.builder()
                    .applicationType(C1_WITH_SUPPLEMENT).build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(callbackResponse.getData().get("c2Type")).isNull();
        assertTemporaryFieldsAreRemoved(caseData);
    }

    @Test
    void shouldUpdateOldC2DocumentBundleCollection() {
        C2DocumentBundle firstBundleAdded = C2DocumentBundle.builder()
            .type(WITHOUT_NOTICE)
            .uploadedDateTime("14 December 2020, 4:24pm")
            .document(DocumentReference.builder()
                .filename("Document 1")
                .build()).build();

        C2DocumentBundle secondBundleAdded = C2DocumentBundle.builder()
            .type(WITH_NOTICE)
            .uploadedDateTime("15 December 2020, 4:24pm")
            .document(DocumentReference.builder()
                .filename("Document 2")
                .build()).build();

        C2DocumentBundle thirdBundleAdded = C2DocumentBundle.builder()
            .type(WITH_NOTICE)
            .uploadedDateTime("16 December 2020, 4:24pm")
            .document(DocumentReference.builder()
                .filename("Document 3")
                .build()).build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "caseLocalAuthorityName", LOCAL_AUTHORITY_NAME,
                "c2DocumentBundle", wrapElements(firstBundleAdded, secondBundleAdded, thirdBundleAdded),
                "applicantsList", createApplicantsDynamicList(APPLICANT)))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        List<Element<C2DocumentBundle>> expectedC2DocumentBundle = wrapElements(thirdBundleAdded, secondBundleAdded,
            firstBundleAdded);

        assertThat(caseData.getC2DocumentBundle()).isEqualTo(expectedC2DocumentBundle);
    }

    private void assertC2DocumentBundle(C2DocumentBundle uploadedC2DocumentBundle) {
        String expectedDateTime = formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME);

        assertThat(uploadedC2DocumentBundle.getUploadedDateTime()).isEqualTo(expectedDateTime);

        assertThat(uploadedC2DocumentBundle.getAuthor()).isEqualTo(USER_NAME);
        assertDocument(uploadedC2DocumentBundle.getDocument(), sealedDocument);
        assertSupportingEvidenceBundle(uploadedC2DocumentBundle.getSupportingEvidenceBundle());
        assertSupplementsBundle(uploadedC2DocumentBundle.getSupplementsBundle());
    }

    private void assertOtherApplicationsBundle(OtherApplicationsBundle uploadedOtherApplicationsBundle) {
        String expectedDateTime = formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME);

        assertThat(uploadedOtherApplicationsBundle.getUploadedDateTime()).isEqualTo(expectedDateTime);

        assertThat(uploadedOtherApplicationsBundle.getApplicationType())
            .isEqualTo(OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN);
        assertThat(uploadedOtherApplicationsBundle.getAuthor()).isEqualTo(USER_NAME);

        assertSupportingEvidenceBundle(uploadedOtherApplicationsBundle.getSupportingEvidenceBundle());
        assertSupplementsBundle(uploadedOtherApplicationsBundle.getSupplementsBundle());

        assertThat(uploadedOtherApplicationsBundle.getDocument()).isEqualTo(sealedDocument);
    }

    private void assertTemporaryFieldsAreRemoved(CaseData caseData) {
        assertThat(caseData.getTemporaryC2Document()).isNull();
        assertThat(caseData.getTemporaryOtherApplicationsBundle()).isNull();
        assertThat(caseData.getTemporaryPbaPayment()).isNull();
        assertThat(caseData.getC2ApplicationType()).isNull();
        assertThat(caseData.getAmountToPay()).isNull();
        assertThat(caseData.getApplicantsList()).isNull();
        assertThat(caseData.getOtherApplicant()).isNull();
    }

    private void assertDocument(DocumentReference actualDocument, DocumentReference expectedDocument) {
        assertThat(actualDocument.getUrl()).isEqualTo(expectedDocument.getUrl());
        assertThat(actualDocument.getFilename()).isEqualTo(expectedDocument.getFilename());
        assertThat(actualDocument.getBinaryUrl()).isEqualTo(expectedDocument.getBinaryUrl());
    }

    private void assertSupportingEvidenceBundle(List<Element<SupportingEvidenceBundle>> documentBundle) {
        List<SupportingEvidenceBundle> supportingEvidenceBundle = unwrapElements(documentBundle);

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
            uploadedDocument,
            USER_NAME
        );
    }

    private void assertSupplementsBundle(List<Element<Supplement>> documentBundle) {
        List<Supplement> supplementsBundle = unwrapElements(documentBundle);

        assertThat(supplementsBundle).first()
            .extracting(
                Supplement::getName,
                Supplement::getNotes,
                Supplement::getDateTimeUploaded,
                Supplement::getDocument,
                Supplement::getUploadedBy
            ).containsExactly(
            SupplementType.C13A_SPECIAL_GUARDIANSHIP,
            "Supplement notes",
            time.now(),
            sealedDocument,
            USER_NAME
        );
    }

    private PBAPayment createPbaPayment() {
        return PBAPayment.builder().pbaNumber("PBA1234567").usePbaPayment("Yes").build();
    }

    private Map<String, Object> createTemporaryC2Document() {
        return Map.of(
            "temporaryC2Document", Map.of(
                "type", C2ApplicationType.WITH_NOTICE,
                "document", uploadedDocument,
                "supportingEvidenceBundle", wrapElements(createSupportingEvidenceBundle()),
                "supplementsBundle", wrapElements(createSupplementsBundle())
            )
        );
    }

    private DynamicList createApplicantsDynamicList(String selected) {
        DynamicListElement applicant = DynamicListElement.builder()
            .code(APPLICANT).label(LOCAL_AUTHORITY_NAME).build();

        DynamicListElement other = DynamicListElement.builder()
            .code(APPLICANT_SOMEONE_ELSE).label("Someone else").build();

        return DynamicList.builder()
            .value(APPLICANT.equals(selected) ? applicant : other)
            .listItems(List.of(applicant, other)).build();
    }

    private Map<String, Object> createTemporaryOtherApplicationDocument() {
        return Map.of(
            "temporaryOtherApplicationsBundle", Map.of(
                "applicationType", OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN.name(),
                "document", uploadedDocument,
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
            .document(uploadedDocument)
            .build();
    }

    private Supplement createSupplementsBundle() {
        return Supplement.builder()
            .name(SupplementType.C13A_SPECIAL_GUARDIANSHIP)
            .notes("Supplement notes")
            .dateTimeUploaded(time.now())
            .document(uploadedDocument)
            .build();
    }

    private UserDetails createUserDetailsWithHmctsRole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Hudson")
            .forename("Steve")
            .email("steve.hudson@gov.uk")
            .roles(asList(HMCTS_ADMIN.getRoleName(), JUDICIARY.getRoleName()))
            .build();
    }

}
