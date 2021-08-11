package uk.gov.hmcts.reform.fpl.controllers.documents;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentType;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.representativeSolicitors;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.ADDITIONAL_APPLICATIONS_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final String USER = "HMCTS";
    private static final String[] USER_ROLES = {"caseworker-publiclaw-courtadmin", "caseworker-publiclaw-judiciary"};
    private static final SupportingEvidenceBundle NON_CONFIDENTIAL_BUNDLE = SupportingEvidenceBundle.builder()
        .dateTimeUploaded(LocalDateTime.now())
        .uploadedBy(USER)
        .name("test")
        .build();
    private static final SupportingEvidenceBundle CONFIDENTIAL_BUNDLE = SupportingEvidenceBundle.builder()
        .dateTimeUploaded(LocalDateTime.now())
        .uploadedBy(USER)
        .name("confidential test")
        .confidential(List.of("CONFIDENTIAL"))
        .build();
    private static final long CASE_ID = 12345L;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private UserService userService;

    ManageDocumentsControllerAboutToSubmitTest() {
        super("manage-documents");
    }

    @BeforeEach
    void init() {
        givenCurrentUser(createUserDetailsWithHmctsRole());
        given(userService.hasAnyCaseRoleFrom(representativeSolicitors(), CASE_ID)).willReturn(false);
    }

    @Test
    void shouldPopulateHearingFurtherDocumentsCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .manageDocumentsHearingList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(hearingId)
                    .build())
                .build())
            .hearingDetails(List.of(element(hearingId, hearingBooking)))
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS))
            .build();

        given(featureToggleService.isFurtherEvidenceDocumentTabEnabled()).willReturn(true);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData, USER_ROLES);
        CaseData extractedCaseData = extractCaseData(response);

        assertThat(extractedCaseData.getHearingFurtherEvidenceDocuments()).first()
            .extracting(evidence -> evidence.getValue().getSupportingEvidenceBundle())
            .isEqualTo(furtherEvidenceBundle);

        assertThat((String) response.getData().get("documentViewLA")).isNotEmpty();
        assertThat((String) response.getData().get("documentViewHMCTS")).isNotEmpty();
        assertThat((String) response.getData().get("documentViewNC")).isNotEmpty();
        assertThat((String) response.getData().get("showFurtherEvidenceTab")).isEqualTo("YES");

        assertExpectedFieldsAreRemoved(extractedCaseData);
    }

    @Test
    void shouldPopulateFurtherDocumentsCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocumentsRelatedToHearing(NO.getValue())
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS))
            .build();

        given(featureToggleService.isFurtherEvidenceDocumentTabEnabled()).willReturn(false);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData, USER_ROLES);
        CaseData extractedCaseData = extractCaseData(response);

        assertThat(extractedCaseData.getFurtherEvidenceDocuments()).isEqualTo(furtherEvidenceBundle);
        assertExpectedFieldsAreRemoved(extractedCaseData);

        assertThat(response.getData().get("documentViewLA")).isNull();
        assertThat(response.getData().get("documentViewHMCTS")).isNull();
        assertThat(response.getData().get("documentViewNC")).isNull();
        assertThat((String) response.getData().get("showFurtherEvidenceTab")).isNull();
    }

    @Test
    void shouldPopulateCorrespondenceEvidenceCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = wrapElements(
            SupportingEvidenceBundle.builder()
                .name("test1")
                .dateTimeUploaded(now().minusDays(2))
                .uploadedBy(USER)
                .build(),
            SupportingEvidenceBundle.builder()
                .name("test2")
                .dateTimeUploaded(now())
                .uploadedBy(USER)
                .build());

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocument(buildManagementDocument(CORRESPONDENCE))
            .build();

        given(featureToggleService.isFurtherEvidenceDocumentTabEnabled()).willReturn(true);
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData, USER_ROLES);
        CaseData extractedCaseData = extractCaseData(response);

        List<Element<SupportingEvidenceBundle>> expectedDocuments = List.of(
            furtherEvidenceBundle.get(1), furtherEvidenceBundle.get(0));

        assertThat(extractedCaseData.getCorrespondenceDocuments()).isEqualTo(expectedDocuments);
        assertExpectedFieldsAreRemoved(extractedCaseData);

        assertThat(response.getData().get("documentViewHMCTS")).isNull();
        assertThat(response.getData().get("documentViewLA")).isNull();
        assertThat(response.getData().get("documentViewNC")).isNull();
        assertThat((String) response.getData().get("showFurtherEvidenceTab")).isEqualTo("NO");
    }

    @Test
    void shouldPopulateC2DocumentBundleCollectionWhenSelectedApplicationIsInC2DocumentsBundle() {
        UUID selectedC2DocumentId = randomUUID();
        C2DocumentBundle selectedC2DocumentBundle = buildC2DocumentBundle(now().plusDays(2));
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = buildSupportingEvidenceBundle(
            now().plusDays(3));

        Element<C2DocumentBundle> c2BundleElement = element(buildC2DocumentBundle(now().plusDays(2)));
        Element<C2DocumentBundle> selectedBundle = element(selectedC2DocumentId, selectedC2DocumentBundle);
        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(selectedBundle, c2BundleElement);

        C2DocumentBundle c2Application = C2DocumentBundle.builder().id(randomUUID())
            .uploadedDateTime(LocalDateTime.now().toString()).build();

        OtherApplicationsBundle otherApplication = OtherApplicationsBundle.builder()
            .id(randomUUID()).applicationType(OtherApplicationType.C1_WITH_SUPPLEMENT)
            .uploadedDateTime(LocalDateTime.now().toString()).build();

        DynamicList expectedDynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().code(selectedC2DocumentId).build()).build();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .c2DocumentBundle(c2DocumentBundleList)
            .manageDocumentsSupportingC2List(expectedDynamicList)
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(c2Application).otherApplicationsBundle(otherApplication).build()))
            .supportingEvidenceDocumentsTemp(supportingEvidenceBundle)
            .manageDocument(buildManagementDocument(ADDITIONAL_APPLICATIONS_DOCUMENTS))
            .build();

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseData, USER_ROLES));

        C2DocumentBundle expectedBundle = selectedBundle.getValue().toBuilder()
            .supportingEvidenceBundle(supportingEvidenceBundle).build();
        assertThat(extractedCaseData.getC2DocumentBundle().get(0).getValue()).isEqualTo(expectedBundle);
        assertExpectedFieldsAreRemoved(extractedCaseData);
    }

    @Test
    void shouldPopulateC2DocumentBundleWhenSelectedApplicationIsInAdditionalApplicationsBundle() {
        UUID selectedC2DocumentId = randomUUID();
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle
            = buildSupportingEvidenceBundle(now().plusDays(3));

        Element<C2DocumentBundle> c2BundleElement = element(buildC2DocumentBundle(now().plusDays(2)));
        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(c2BundleElement);

        C2DocumentBundle selectedC2Application = C2DocumentBundle.builder().id(selectedC2DocumentId)
            .uploadedDateTime(LocalDateTime.now().toString()).build();

        OtherApplicationsBundle otherApplication = OtherApplicationsBundle.builder()
            .id(randomUUID()).applicationType(OtherApplicationType.C1_WITH_SUPPLEMENT)
            .uploadedDateTime(LocalDateTime.now().toString()).build();

        DynamicList expectedDynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().code(selectedC2DocumentId).build()).build();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .c2DocumentBundle(c2DocumentBundleList)
            .manageDocumentsSupportingC2List(expectedDynamicList)
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(selectedC2Application).otherApplicationsBundle(otherApplication).build()))
            .supportingEvidenceDocumentsTemp(supportingEvidenceBundle)
            .manageDocument(buildManagementDocument(ADDITIONAL_APPLICATIONS_DOCUMENTS))
            .build();

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseData, USER_ROLES));

        C2DocumentBundle expectedBundle = selectedC2Application.toBuilder()
            .supportingEvidenceBundle(supportingEvidenceBundle).build();

        assertThat(extractedCaseData.getAdditionalApplicationsBundle().get(0).getValue().getC2DocumentBundle())
            .isEqualTo(expectedBundle);
        assertExpectedFieldsAreRemoved(extractedCaseData);
    }

    @Test
    void shouldPopulateOtherApplicationBundleWhenSelectedApplicationIsInAdditionalApplicationsBundle() {
        UUID selectedBundleId = randomUUID();
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = buildSupportingEvidenceBundle(
            now().plusDays(3));

        C2DocumentBundle c2Application = C2DocumentBundle.builder().id(randomUUID())
            .uploadedDateTime(LocalDateTime.now().toString()).build();

        OtherApplicationsBundle selectedOtherApplication = OtherApplicationsBundle.builder().id(selectedBundleId)
            .applicationType(OtherApplicationType.C1_WITH_SUPPLEMENT)
            .uploadedDateTime(LocalDateTime.now().toString()).build();

        DynamicList expectedDynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().code(selectedBundleId).build()).build();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .manageDocumentsSupportingC2List(expectedDynamicList)
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(c2Application).otherApplicationsBundle(selectedOtherApplication).build()))
            .supportingEvidenceDocumentsTemp(supportingEvidenceBundle)
            .manageDocument(buildManagementDocument(ADDITIONAL_APPLICATIONS_DOCUMENTS))
            .build();

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseData, USER_ROLES));

        OtherApplicationsBundle expectedBundle = selectedOtherApplication.toBuilder()
            .supportingEvidenceBundle(supportingEvidenceBundle).build();

        assertThat(extractedCaseData.getAdditionalApplicationsBundle().get(0).getValue().getOtherApplicationsBundle())
            .isEqualTo(expectedBundle);
        assertExpectedFieldsAreRemoved(extractedCaseData);
    }

    @Test
    void shouldDuplicateNonConfidentialCorrespondenceDocsInAnotherList() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = wrapElements(
            CONFIDENTIAL_BUNDLE, NON_CONFIDENTIAL_BUNDLE
        );

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocument(buildManagementDocument(CORRESPONDENCE))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData, USER_ROLES);
        CaseData extractedCaseData = extractCaseData(response);

        List<Element<SupportingEvidenceBundle>> correspondenceDocumentsNC =
            mapper.convertValue(response.getData().get("correspondenceDocumentsNC"), new TypeReference<>() {
            });

        assertThat(extractedCaseData.getCorrespondenceDocuments()).isEqualTo(furtherEvidenceBundle);
        assertThat(correspondenceDocumentsNC).isEqualTo(wrapElements(NON_CONFIDENTIAL_BUNDLE));
    }

    @Test
    void shouldDuplicateNonConfidentialFurtherEvidenceDocsInAnotherList() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = wrapElements(
            CONFIDENTIAL_BUNDLE, NON_CONFIDENTIAL_BUNDLE
        );

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData, USER_ROLES);
        CaseData extractedCaseData = extractCaseData(response);

        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsNC =
            mapper.convertValue(response.getData().get("furtherEvidenceDocumentsNC"), new TypeReference<>() {
            });

        assertThat(extractedCaseData.getFurtherEvidenceDocuments()).isEqualTo(furtherEvidenceBundle);
        assertThat(furtherEvidenceDocumentsNC).isEqualTo(wrapElements(NON_CONFIDENTIAL_BUNDLE));
    }

    private HearingBooking buildFinalHearingBooking() {
        return HearingBooking.builder()
            .type(HearingType.FINAL)
            .startDate(LocalDateTime.now())
            .build();
    }

    private ManageDocument buildManagementDocument(ManageDocumentType type) {
        return ManageDocument.builder().type(type).build();
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
        return wrapElements(SupportingEvidenceBundle.builder()
            .dateTimeUploaded(LocalDateTime.now())
            .type(FurtherEvidenceType.APPLICANT_STATEMENT)
            .uploadedBy(USER)
            .document(DocumentReference.builder().binaryUrl("binary-url").url("fake-url").filename("file1").build())
            .name("test")
            .build());
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle(LocalDateTime localDateTime) {
        return wrapElements(SupportingEvidenceBundle.builder()
            .name("test")
            .dateTimeUploaded(localDateTime)
            .uploadedBy(USER)
            .build());
    }

    private void assertExpectedFieldsAreRemoved(CaseData caseData) {
        assertThat(caseData.getSupportingEvidenceDocumentsTemp()).isEmpty();
        assertThat(caseData.getManageDocument()).isNull();
        assertThat(caseData.getC2SupportingDocuments()).isNull();
        assertThat(caseData.getManageDocumentsHearingList()).isNull();
        assertThat(caseData.getManageDocumentsSupportingC2List()).isNull();
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString()).build();
    }

    private UserDetails createUserDetailsWithHmctsRole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Hudson")
            .forename("Steve")
            .email("steve.hudson@gov.uk")
            .roles(Arrays.asList(USER_ROLES))
            .build();
    }
}
