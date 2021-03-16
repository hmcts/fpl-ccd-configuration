package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
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
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.USER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_PARENTAL_RESPONSIBILITY;
import static uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType.PR_BY_FATHER;
import static uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType.WALES;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C20_SECURE_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    UploadAdditionalApplicationsService.class,
    FixedTimeConfiguration.class,
    DocumentUploadHelper.class
})
class UploadAdditionalApplicationsServiceTest {
    private static final String USER_ID = "1";
    public static final String HMCTS = "HMCTS";
    public static final DocumentReference DOCUMENT = testDocumentReference("TestDocument.doc");
    public static final DocumentReference SUPPLEMENT_DOCUMENT = testDocumentReference("SupplementFile.doc");
    public static final DocumentReference SUPPORTING_DOCUMENT = testDocumentReference("SupportingEvidenceFile.doc");

    @Autowired
    private UploadAdditionalApplicationsService service;

    @Autowired
    private Time time;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private RequestData requestData;

    @BeforeEach()
    void init() {
        given(idamClient.getUserInfo(USER_AUTH_TOKEN)).willReturn(UserInfo.builder().name("Emma Taylor").build());
        given(idamClient.getUserDetails(USER_AUTH_TOKEN)).willReturn(createUserDetailsWithHmctsRole());
        given(requestData.authorisation()).willReturn(USER_AUTH_TOKEN);
    }

    @Test
    void shouldBuildExpectedC2DocumentBundle() {
        Supplement supplement = createSupplementsBundle();
        SupportingEvidenceBundle supportingEvidenceBundle = createSupportingEvidenceBundle();

        CaseData caseData = CaseData.builder()
            .temporaryC2Document(createC2DocumentBundle(supplement, supportingEvidenceBundle))
            .c2Type(WITH_NOTICE)
            .build();

        C2DocumentBundle actualC2DocumentBundle = service.buildC2DocumentBundle(caseData);

        assertC2Bundle(actualC2DocumentBundle);
        assertSupplementsBundle(actualC2DocumentBundle.getSupplementsBundle().get(0).getValue(), supplement);
        assertSupportingEvidenceBundle(
            actualC2DocumentBundle.getSupportingEvidenceBundle().get(0).getValue(), supportingEvidenceBundle);
    }

    @Test
    void shouldBuildOtherApplicationsBundle() {
        Supplement supplement = createSupplementsBundle();
        SupportingEvidenceBundle supportingDocument = createSupportingEvidenceBundle();

        OtherApplicationsBundle otherApplicationsBundle = createOtherApplicationsBundle(
            supplement, supportingDocument);

        OtherApplicationsBundle actualOtherApplicationsBundle = service.buildOtherApplicationsBundle(
            CaseData.builder().temporaryOtherApplicationsBundle(otherApplicationsBundle).build());

        assertOtherApplicationsBundle(actualOtherApplicationsBundle);
        assertSupplementsBundle(
            actualOtherApplicationsBundle.getSupplementsBundle().get(0).getValue(), supplement);
        assertSupportingEvidenceBundle(
            actualOtherApplicationsBundle.getSupportingEvidenceBundle().get(0).getValue(), supportingDocument);
    }

    @Test
    void shouldBuildAdditionalApplicationsBundle() {
        Supplement c2Supplement = createSupplementsBundle();
        SupportingEvidenceBundle c2SupportingDocument = createSupportingEvidenceBundle();

        Supplement otherSupplementsBundle = createSupplementsBundle(C20_SECURE_ACCOMMODATION);
        SupportingEvidenceBundle otherSupportingDocument = createSupportingEvidenceBundle("other document");

        C2DocumentBundle c2DocumentBundle = createC2DocumentBundle(c2Supplement, c2SupportingDocument);

        OtherApplicationsBundle otherApplicationsBundle = createOtherApplicationsBundle(
            otherSupplementsBundle, otherSupportingDocument);

        PBAPayment pbaPayment = buildPBAPayment();

        AdditionalApplicationsBundle actual = service.buildAdditionalApplicationsBundle(
            CaseData.builder().temporaryPbaPayment(pbaPayment).build(),
            c2DocumentBundle,
            otherApplicationsBundle);

        assertThat(actual).extracting("author", "c2DocumentBundle", "otherApplicationsBundle", "pbaPayment")
            .containsExactly(HMCTS, c2DocumentBundle, otherApplicationsBundle, pbaPayment);
    }

    @Test
    void shouldBuildAdditionalApplicationsBundleWithC2DocumentBundleAndPBAPayment() {
        Supplement c2Supplement = createSupplementsBundle(C20_SECURE_ACCOMMODATION);
        SupportingEvidenceBundle c2SupportingDocument = createSupportingEvidenceBundle();

        C2DocumentBundle c2DocumentBundle = createC2DocumentBundle(c2Supplement, c2SupportingDocument);

        PBAPayment pbaPayment = buildPBAPayment();

        AdditionalApplicationsBundle actual = service.buildAdditionalApplicationsBundle(
            CaseData.builder().temporaryPbaPayment(pbaPayment).build(), c2DocumentBundle, null);

        assertThat(actual).extracting("author", "c2DocumentBundle", "otherApplicationsBundle", "pbaPayment")
            .containsExactly(HMCTS, c2DocumentBundle, null, pbaPayment);
    }

    @Test
    void shouldBuildAdditionalApplicationsBundleWithOtherDocumentBundleAndPBAPayment() {
        Supplement supplement = createSupplementsBundle(SupplementType.C16_CHILD_ASSESSMENT);
        SupportingEvidenceBundle supportingDocument = createSupportingEvidenceBundle();

        OtherApplicationsBundle otherApplicationsBundle = createOtherApplicationsBundle(
            supplement, supportingDocument);

        PBAPayment pbaPayment = buildPBAPayment();

        AdditionalApplicationsBundle actual = service.buildAdditionalApplicationsBundle(
            CaseData.builder().temporaryPbaPayment(pbaPayment).build(), null, otherApplicationsBundle);

        assertThat(actual).extracting("author", "c2DocumentBundle", "otherApplicationsBundle", "pbaPayment")
            .containsExactly(HMCTS, null, otherApplicationsBundle, pbaPayment);
    }

    @Test
    void shouldSortOldC2DocumentBundlesToDateDescending() {
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

        List<Element<C2DocumentBundle>> oldC2DocumentBundle = Arrays.asList(element(firstBundleAdded),
            element(secondBundleAdded), element(thirdBundleAdded));

        List<Element<C2DocumentBundle>> actualC2DocumentBundleList = service.sortOldC2DocumentCollection(oldC2DocumentBundle);
        C2DocumentBundle bundleAtFirstIndex = actualC2DocumentBundleList.get(0).getValue();
        C2DocumentBundle bundleAtLastIndex = actualC2DocumentBundleList.get(2).getValue();

        assertThat(bundleAtFirstIndex.getUploadedDateTime()).isEqualTo(thirdBundleAdded
            .getUploadedDateTime());
        assertThat(bundleAtLastIndex.getUploadedDateTime()).isEqualTo(firstBundleAdded
            .getUploadedDateTime());
    }

    private void assertC2Bundle(C2DocumentBundle documentBundle) {
        assertThat(documentBundle.getDocument().getFilename()).isEqualTo(DOCUMENT.getFilename());
        assertThat(documentBundle.getType()).isEqualTo(WITH_NOTICE);
        assertThat(documentBundle.getSupportingEvidenceBundle()).hasSize(1);
        assertThat(documentBundle.getSupplementsBundle()).hasSize(1);
    }

    private void assertOtherApplicationsBundle(OtherApplicationsBundle documentBundle) {
        assertThat(documentBundle.getDocument().getFilename()).isEqualTo(DOCUMENT.getFilename());
        assertThat(documentBundle.getApplicationType()).isEqualTo(C1_PARENTAL_RESPONSIBILITY);
        assertThat(documentBundle.getParentalResponsibilityType()).isEqualTo(PR_BY_FATHER);
        assertThat(documentBundle.getAuthor()).isEqualTo(HMCTS);
        assertThat(documentBundle.getSupportingEvidenceBundle()).hasSize(1);
        assertThat(documentBundle.getSupplementsBundle()).hasSize(1);
    }

    private void assertSupplementsBundle(Supplement actual, Supplement expected) {
        assertThat(actual)
            .extracting("name", "notes", "document", "uploadedBy")
            .containsExactly(expected.getName(), expected.getNotes(), expected.getDocument(), HMCTS);
    }

    private void assertSupportingEvidenceBundle(SupportingEvidenceBundle actual, SupportingEvidenceBundle expected) {
        assertThat(actual)
            .extracting("name", "notes", "document", "uploadedBy")
            .containsExactly(expected.getName(), expected.getNotes(), expected.getDocument(), HMCTS);
    }

    private PBAPayment buildPBAPayment() {
        return PBAPayment.builder().usePbaPayment("Yes").usePbaPayment("PBA12345").build();
    }

    private OtherApplicationsBundle createOtherApplicationsBundle(
        Supplement supplement, SupportingEvidenceBundle supportingDocument1) {
        return OtherApplicationsBundle.builder()
            .applicationType(C1_PARENTAL_RESPONSIBILITY)
            .document(DOCUMENT)
            .parentalResponsibilityType(PR_BY_FATHER)
            .supplementsBundle(wrapElements(supplement))
            .supportingEvidenceBundle(wrapElements(supportingDocument1))
            .build();
    }

    private C2DocumentBundle createC2DocumentBundle(
        Supplement supplementsBundle, SupportingEvidenceBundle supportingEvidenceBundle) {
        return C2DocumentBundle.builder()
            .type(WITH_NOTICE)
            .document(DOCUMENT)
            .c2AdditionalOrdersRequested(List.of(C2AdditionalOrdersRequested.PARENTAL_RESPONSIBILITY))
            .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_SECOND_FEMALE_PARENT)
            .supportingEvidenceBundle(wrapElements(supportingEvidenceBundle))
            .supplementsBundle(wrapElements(supplementsBundle))
            .build();
    }

    private SupportingEvidenceBundle createSupportingEvidenceBundle() {
        return createSupportingEvidenceBundle("Supporting document");
    }

    private SupportingEvidenceBundle createSupportingEvidenceBundle(String name) {
        return SupportingEvidenceBundle.builder()
            .name(name)
            .notes("Document notes")
            .document(SUPPORTING_DOCUMENT)
            .dateTimeReceived(time.now().minusDays(1))
            .build();
    }

    private Supplement createSupplementsBundle() {
        return createSupplementsBundle(C13A_SPECIAL_GUARDIANSHIP);
    }

    private Supplement createSupplementsBundle(SupplementType name) {
        return Supplement.builder()
            .name(name)
            .secureAccommodationType(name == C20_SECURE_ACCOMMODATION ? WALES : null)
            .notes("Document notes")
            .document(SUPPLEMENT_DOCUMENT)
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
