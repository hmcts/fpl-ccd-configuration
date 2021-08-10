package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.enums.SupplementType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
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
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.PeopleInCaseService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.USER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.C2_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.OTHER_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
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
    private static final String HMCTS = "HMCTS";

    private static final DocumentReference DOCUMENT = testDocumentReference("TestDocument.doc");
    private static final DocumentReference SEALED_CONVERTED_DOCUMENT = testDocumentReference("TestDocument.pdf");

    private static final DocumentReference SUPPLEMENT_DOCUMENT = testDocumentReference("SupplementFile.doc");
    private static final DocumentReference SEALED_SUPPLEMENT_DOCUMENT = testDocumentReference("SupplementFile.pdf");

    private static final DocumentReference SUPPORTING_DOCUMENT = testDocumentReference("SupportingEvidenceFile.doc");

    @Autowired
    private Time time;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private RequestData requestData;

    @MockBean
    private DocumentSealingService documentSealingService;

    @MockBean
    private PeopleInCaseService peopleInCaseService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private UploadAdditionalApplicationsService underTest;

    public static final String APPLICANT_NAME = "Swansea local authority, Applicant";
    public static final String APPLICANT_SOMEONE_ELSE = "SOMEONE_ELSE";

    private static final List<DynamicListElement> DYNAMIC_LIST_ELEMENTS = List.of(
        DynamicListElement.builder().code("applicant").label(APPLICANT_NAME).build(),
        DynamicListElement.builder().code(APPLICANT_SOMEONE_ELSE).label("Someone else").build());

    @BeforeEach()
    void init() {
        given(idamClient.getUserDetails(USER_AUTH_TOKEN)).willReturn(createUserDetailsWithHmctsRole());
        given(requestData.authorisation()).willReturn(USER_AUTH_TOKEN);
        given(documentSealingService.sealDocument(DOCUMENT)).willReturn(SEALED_CONVERTED_DOCUMENT);
        given(documentSealingService.sealDocument(SUPPLEMENT_DOCUMENT)).willReturn(SEALED_SUPPLEMENT_DOCUMENT);
    }

    @Test
    void shouldBuildExpectedC2DocumentBundle() {
        Supplement supplement = createSupplementsBundle();
        SupportingEvidenceBundle supportingEvidenceBundle = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0)).listItems(DYNAMIC_LIST_ELEMENTS).build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(C2_ORDER))
            .temporaryC2Document(createC2DocumentBundle(supplement, supportingEvidenceBundle))
            .temporaryPbaPayment(pbaPayment)
            .applicantsList(applicantsList)
            .c2Type(WITH_NOTICE)
            .build();

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getAuthor()).isEqualTo(HMCTS);
        assertThat(actual.getPbaPayment()).isEqualTo(pbaPayment);
        assertThat(actual.getC2DocumentBundle().getApplicantName()).isEqualTo(APPLICANT_NAME);

        assertC2DocumentBundle(actual.getC2DocumentBundle(), supplement, supportingEvidenceBundle);
    }

    @Test
    void shouldBuildOtherApplicationsBundleWithOtherApplicantName() {
        Supplement supplement = createSupplementsBundle();
        SupportingEvidenceBundle supportingDocument = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();

        // select "Someone else"
        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(1)).listItems(DYNAMIC_LIST_ELEMENTS).build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(OTHER_ORDER))
            .temporaryOtherApplicationsBundle(createOtherApplicationsBundle(supplement, supportingDocument))
            .temporaryPbaPayment(pbaPayment)
            .applicantsList(applicantsList)
            .otherApplicant("some other name")
            .build();

        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);
        given(peopleInCaseService.getSelectedOthers(any())).willReturn(List.of());
        given(peopleInCaseService.getSelectedRespondents(any())).willReturn(List.of());
        given(peopleInCaseService.getPeopleNotified(any(), eq(List.of()), eq(List.of()))).willReturn("");

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getAuthor()).isEqualTo(HMCTS);
        assertThat(actual.getPbaPayment()).isEqualTo(pbaPayment);
        assertThat(actual.getOtherApplicationsBundle().getApplicantName()).isEqualTo("some other name");
        assertThat(actual.getOtherApplicationsBundle().getOthersNotified()).isEmpty();
        assertThat(actual.getOtherApplicationsBundle().getOthers()).isEmpty();
        assertThat(actual.getOtherApplicationsBundle().getRespondents()).isEmpty();

        assertOtherDocumentBundle(actual.getOtherApplicationsBundle(), supplement, supportingDocument);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowIllegalArgumentExceptionWhenApplicantIsNullOrEmpty(String otherApplicantName) {
        // select applicant "Someone else"
        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(1)).listItems(DYNAMIC_LIST_ELEMENTS).build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(OTHER_ORDER))
            .applicantsList(applicantsList)
            .otherApplicant(otherApplicantName)
            .build();

        assertThatThrownBy(() -> underTest.buildAdditionalApplicationsBundle(caseData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Applicant should not be empty");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldBuildAdditionalApplicationsBundleWithC2ApplicationAndOtherApplicationsBundles(
        boolean othersServedToggledOn) {
        Supplement c2Supplement = createSupplementsBundle();
        SupportingEvidenceBundle c2SupportingDocument = createSupportingEvidenceBundle();

        Supplement otherSupplement = createSupplementsBundle(C20_SECURE_ACCOMMODATION);
        SupportingEvidenceBundle otherSupportingDocument = createSupportingEvidenceBundle("other document");
        PBAPayment pbaPayment = buildPBAPayment();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0))
            .listItems(DYNAMIC_LIST_ELEMENTS).build();

        CaseData caseData = CaseData.builder().temporaryPbaPayment(pbaPayment)
            .additionalApplicationType(List.of(C2_ORDER, OTHER_ORDER))
            .c2Type(WITH_NOTICE)
            .temporaryC2Document(createC2DocumentBundle(c2Supplement, c2SupportingDocument))
            .temporaryOtherApplicationsBundle(createOtherApplicationsBundle(otherSupplement, otherSupportingDocument))
            .temporaryPbaPayment(pbaPayment)
            .applicantsList(applicantsList)
            .build();

        List<Element<Other>> selectedOthers = wrapElements(
            Other.builder().name("Other1").address(Address.builder().postcode("SE1").build()).build(),
            Other.builder().name("Other2").address(Address.builder().postcode("SE2").build()).build());

        List<Element<Respondent>> selectedRespondents = wrapElements(
            Respondent.builder().party(
                RespondentParty.builder().firstName("First").lastName("Respondent")
                    .address(Address.builder().postcode("SE1").build()).build()).build());

        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(othersServedToggledOn);

        String othersNotified = "First Respondent, Other1, Other2";
        if (othersServedToggledOn) {
            given(peopleInCaseService.getSelectedOthers(any())).willReturn(selectedOthers);
            given(peopleInCaseService.getSelectedRespondents(any())).willReturn(selectedRespondents);
            given(peopleInCaseService.getPeopleNotified(any(), eq(selectedRespondents), eq(selectedOthers)))
                .willReturn(othersNotified);
        }

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getAuthor()).isEqualTo(HMCTS);
        assertThat(actual.getPbaPayment()).isEqualTo(pbaPayment);
        assertThat(actual.getC2DocumentBundle().getApplicantName()).isEqualTo(APPLICANT_NAME);
        assertThat(actual.getOtherApplicationsBundle().getApplicantName()).isEqualTo(APPLICANT_NAME);

        if (othersServedToggledOn) {
            assertThat(actual.getC2DocumentBundle().getOthers()).isEqualTo(selectedOthers);
            assertThat(actual.getC2DocumentBundle().getOthersNotified()).isEqualTo(othersNotified);
            assertThat(actual.getOtherApplicationsBundle().getOthers()).isEqualTo(selectedOthers);
            assertThat(actual.getOtherApplicationsBundle().getOthersNotified()).isEqualTo(othersNotified);
        } else {
            assertThat(actual.getC2DocumentBundle().getOthers()).isNull();
            assertThat(actual.getC2DocumentBundle().getOthersNotified()).isNull();
            assertThat(actual.getOtherApplicationsBundle().getOthers()).isNull();
            assertThat(actual.getOtherApplicationsBundle().getOthersNotified()).isNull();
        }

        assertC2DocumentBundle(actual.getC2DocumentBundle(), c2Supplement, c2SupportingDocument);
        assertOtherDocumentBundle(actual.getOtherApplicationsBundle(), otherSupplement, otherSupportingDocument);
    }

    private void assertC2DocumentBundle(
        C2DocumentBundle actualC2Bundle,
        Supplement expectedSupplement,
        SupportingEvidenceBundle expectedSupportingEvidence
    ) {
        assertThat(actualC2Bundle.getId()).isNotNull();
        assertThat(actualC2Bundle.getDocument().getFilename()).isEqualTo(SEALED_CONVERTED_DOCUMENT.getFilename());
        assertThat(actualC2Bundle.getType()).isEqualTo(WITH_NOTICE);
        assertThat(actualC2Bundle.getSupportingEvidenceBundle()).hasSize(1);
        assertThat(actualC2Bundle.getSupplementsBundle()).hasSize(1);

        assertSupplementsBundle(actualC2Bundle.getSupplementsBundle().get(0).getValue(), expectedSupplement);
        assertSupportingEvidenceBundle(
            actualC2Bundle.getSupportingEvidenceBundle().get(0).getValue(), expectedSupportingEvidence);
    }

    private void assertOtherDocumentBundle(
        OtherApplicationsBundle actual,
        Supplement expectedSupplement,
        SupportingEvidenceBundle expectedSupportingDocument
    ) {
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getDocument().getFilename()).isEqualTo(SEALED_CONVERTED_DOCUMENT.getFilename());
        assertThat(actual.getApplicationType()).isEqualTo(C1_PARENTAL_RESPONSIBILITY);
        assertThat(actual.getParentalResponsibilityType()).isEqualTo(PR_BY_FATHER);
        assertThat(actual.getAuthor()).isEqualTo(HMCTS);
        assertThat(actual.getSupportingEvidenceBundle()).hasSize(1);
        assertThat(actual.getSupplementsBundle()).hasSize(1);

        assertSupplementsBundle(actual.getSupplementsBundle().get(0).getValue(), expectedSupplement);
        assertSupportingEvidenceBundle(
            actual.getSupportingEvidenceBundle().get(0).getValue(), expectedSupportingDocument);
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

        List<Element<C2DocumentBundle>> actualC2DocumentBundleList = underTest.sortOldC2DocumentCollection(
            oldC2DocumentBundle);
        C2DocumentBundle bundleAtFirstIndex = actualC2DocumentBundleList.get(0).getValue();
        C2DocumentBundle bundleAtLastIndex = actualC2DocumentBundleList.get(2).getValue();

        assertThat(bundleAtFirstIndex.getUploadedDateTime()).isEqualTo(thirdBundleAdded
            .getUploadedDateTime());
        assertThat(bundleAtLastIndex.getUploadedDateTime()).isEqualTo(firstBundleAdded
            .getUploadedDateTime());
    }

    @ParameterizedTest
    @MethodSource("additionalApplicationBundlesData")
    void shouldGetTheApplicantAndApplicationTypes(AdditionalApplicationsBundle applicationBundle,
                                                  List<ApplicationType> expectedApplicationTypes) {
        assertThat(underTest.getApplicationTypes(applicationBundle)).isEqualTo(expectedApplicationTypes);
    }

    private static Stream<Arguments> additionalApplicationBundlesData() {
        return Stream.of(
            Arguments.of(AdditionalApplicationsBundle.builder().c2DocumentBundle(
                C2DocumentBundle.builder()
                    .type(WITHOUT_NOTICE)
                    .document(DocumentReference.builder().build()).build()).build(),
                List.of(C2_APPLICATION)),
            Arguments.of(AdditionalApplicationsBundle.builder().otherApplicationsBundle(
                OtherApplicationsBundle.builder()
                    .applicationType(C1_PARENTAL_RESPONSIBILITY)
                    .document(DocumentReference.builder().build()).build()).build(),
                List.of(ApplicationType.C1_PARENTAL_RESPONSIBILITY)),
            Arguments.of(AdditionalApplicationsBundle.builder().c2DocumentBundle(
                C2DocumentBundle.builder()
                    .type(WITH_NOTICE)
                    .document(DocumentReference.builder().build()).build())
                    .otherApplicationsBundle(
                        OtherApplicationsBundle.builder()
                            .applicationType(C1_PARENTAL_RESPONSIBILITY)
                            .document(DocumentReference.builder().build()).build()).build(),
                List.of(C2_APPLICATION, ApplicationType.C1_PARENTAL_RESPONSIBILITY))
        );
    }

    private void assertSupplementsBundle(Supplement actual, Supplement exampleOfExpectedSupplement) {
        Supplement expectedSupplement = exampleOfExpectedSupplement.toBuilder()
            .dateTimeUploaded(time.now())
            .uploadedBy(HMCTS)
            .document(SEALED_SUPPLEMENT_DOCUMENT)
            .build();
        assertThat(actual).isEqualTo(expectedSupplement);
    }

    private void assertSupportingEvidenceBundle(SupportingEvidenceBundle actual, SupportingEvidenceBundle expected) {
        assertThat(actual).isEqualTo(expected.toBuilder()
            .dateTimeUploaded(time.now())
            .uploadedBy(HMCTS)
            .build());
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
