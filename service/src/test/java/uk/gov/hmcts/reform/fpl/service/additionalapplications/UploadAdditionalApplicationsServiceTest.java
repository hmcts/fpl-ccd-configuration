package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.enums.SupplementType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentPolicyData;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.PeopleInCaseService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_1;
import static uk.gov.hmcts.reform.fpl.Constants.USER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.Constants.USER_ID;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.C2_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.OTHER_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested.CHANGE_SURNAME_OR_REMOVE_JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_PARENTAL_RESPONSIBILITY;
import static uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType.PR_BY_FATHER;
import static uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType.WALES;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C20_SECURE_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithRandomUUID;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class UploadAdditionalApplicationsServiceTest {

    private static final String HMCTS = "HMCTS";
    private static final String USER_EMAIL = "user@email.random";

    private static final String APPLICANT_NAME = "Swansea local authority, Applicant";
    private static final String APPLICANT_SOMEONE_ELSE = "SOMEONE_ELSE";

    private static final List<DynamicListElement> DYNAMIC_LIST_ELEMENTS = List.of(
        DynamicListElement.builder().code("applicant").label(APPLICANT_NAME).build(),
        DynamicListElement.builder().code(APPLICANT_SOMEONE_ELSE).label("Someone else").build()
    );

    private static final DocumentReference DOCUMENT = testDocumentReference("TestDocument.doc");
    private static final DocumentReference CONVERTED_DOCUMENT = testDocumentReference("TestDocument.pdf");

    private static final DocumentReference SUPPLEMENT_DOCUMENT = testDocumentReference("SupplementFile.doc");
    private static final DocumentReference CONVERTED_SUPPLEMENT_DOCUMENT = testDocumentReference("SupplementFile.pdf");
    private static final DocumentReference SEALED_SUPPLEMENT_DOCUMENT =
        testDocumentReference("Sealed_SupplementFile.pdf");
    private static final DocumentReference  SEALED_DOCUMENT = testDocumentReference("Sealed_TestDocument.pdf");

    private static final DocumentReference SUPPORTING_DOCUMENT = testDocumentReference("SupportingEvidenceFile.doc");

    private final RequestData requestData = mock(RequestData.class);
    private final Time time = new FixedTimeConfiguration().stoppedTime();
    private final IdamClient idamClient = mock(IdamClient.class);
    private final UserService user = mock(UserService.class);
    private final DocumentUploadHelper uploadHelper = mock(DocumentUploadHelper.class);
    private final DocumentConversionService conversionService = mock(DocumentConversionService.class);
    private final PeopleInCaseService peopleInCaseService = mock(PeopleInCaseService.class);
    private final DocumentSealingService documentSealingService = mock(DocumentSealingService.class);

    private UploadAdditionalApplicationsService underTest;

    @BeforeEach()
    void init() {

        given(idamClient.getUserDetails(USER_AUTH_TOKEN)).willReturn(createUserDetailsWithHmctsRole());
        given(requestData.authorisation()).willReturn(USER_AUTH_TOKEN);
        given(conversionService.convertToPdf(DOCUMENT)).willReturn(CONVERTED_DOCUMENT);
        given(conversionService.convertToPdf(SUPPLEMENT_DOCUMENT)).willReturn(CONVERTED_SUPPLEMENT_DOCUMENT);
        given(documentSealingService.sealDocument(SUPPLEMENT_DOCUMENT, COURT_1, SealType.ENGLISH))
            .willReturn(SEALED_SUPPLEMENT_DOCUMENT);
        given(documentSealingService.sealDocument(DOCUMENT, COURT_1, SealType.ENGLISH))
            .willReturn(SEALED_DOCUMENT);
        underTest = new UploadAdditionalApplicationsService(
            time, user, uploadHelper, documentSealingService, conversionService);
        given(user.isHmctsUser()).willReturn(true);
        given(uploadHelper.getUploadedDocumentUserDetails()).willReturn(HMCTS);
    }

    @Test
    void shouldBuildExpectedC2DocumentBundle() {
        Supplement supplement = createSupplementsBundle();
        SupportingEvidenceBundle supportingEvidenceBundle = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0))
            .listItems(DYNAMIC_LIST_ELEMENTS)
            .build();

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
        assertThat(actual.getApplicationReviewed()).isEqualTo(YesNo.NO);

        assertC2DocumentBundle(actual.getC2DocumentBundle(), supplement, supportingEvidenceBundle);

        // No longer called in this method
        // verify(conversionService).convertToPdf(DOCUMENT);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotConvertApplications(boolean isHmctsUser) {
        given(user.isHmctsUser()).willReturn(isHmctsUser);
        given(uploadHelper.getUploadedDocumentUserDetails()).willReturn(USER_EMAIL);
        given(conversionService.convertToPdf(SUPPLEMENT_DOCUMENT)).willReturn(CONVERTED_SUPPLEMENT_DOCUMENT);
        given(conversionService.convertToPdf(DOCUMENT)).willReturn(CONVERTED_DOCUMENT);

        Supplement supplement = createSupplementsBundle();
        SupportingEvidenceBundle supportingEvidenceBundle = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0))
            .listItems(DYNAMIC_LIST_ELEMENTS)
            .build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(C2_ORDER))
            .temporaryC2Document(createC2DocumentBundle(supplement, supportingEvidenceBundle))
            .temporaryPbaPayment(pbaPayment)
            .applicantsList(applicantsList)
            .c2Type(WITH_NOTICE)
            .build();

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getAuthor()).isEqualTo(USER_EMAIL);
        assertThat(actual.getC2DocumentBundle().getDocument()).isEqualTo(DOCUMENT);
        assertThat(actual.getC2DocumentBundle().getSupplementsBundle()).hasSize(1)
            .first()
            .extracting(actualSupplement -> actualSupplement.getValue().getDocument())
            .isEqualTo(SUPPLEMENT_DOCUMENT);
    }

    @Test
    void shouldBuildOtherApplicationsBundleWithOtherApplicantName() {
        Supplement supplement = createSupplementsBundle();
        SupportingEvidenceBundle supportingDocument = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();

        // select "Someone else"
        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(1))
            .listItems(DYNAMIC_LIST_ELEMENTS)
            .build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(OTHER_ORDER))
            .temporaryOtherApplicationsBundle(createOtherApplicationsBundle(supplement, supportingDocument))
            .temporaryPbaPayment(pbaPayment)
            .applicantsList(applicantsList)
            .otherApplicant("some other name")
            .build();

        given(peopleInCaseService.getSelectedRespondents(any())).willReturn(List.of());
        given(peopleInCaseService.getPeopleNotified(any(), eq(List.of()), eq(List.of()))).willReturn("");

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getAuthor()).isEqualTo(HMCTS);
        assertThat(actual.getPbaPayment()).isEqualTo(pbaPayment);
        assertThat(actual.getOtherApplicationsBundle().getApplicantName()).isEqualTo("some other name");
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

    @Test
    void shouldBuildAdditionalApplicationsBundleWithC2ApplicationAndOtherApplicationsBundles() {
        Supplement c2Supplement = createSupplementsBundle();
        SupportingEvidenceBundle c2SupportingDocument = createSupportingEvidenceBundle();

        Supplement otherSupplement = createSupplementsBundle(C20_SECURE_ACCOMMODATION);
        SupportingEvidenceBundle otherSupportingDocument = createSupportingEvidenceBundle("other document");
        PBAPayment pbaPayment = buildPBAPayment();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0))
            .listItems(DYNAMIC_LIST_ELEMENTS).build();

        List<Element<Respondent>> respondentsInCase = wrapElements(
            Respondent.builder().party(
                RespondentParty.builder().firstName("First").lastName("Respondent")
                    .address(Address.builder().postcode("SE1").build()).build()).build());

        CaseData caseData = CaseData.builder().temporaryPbaPayment(pbaPayment)
            .additionalApplicationType(List.of(C2_ORDER, OTHER_ORDER))
            .c2Type(WITH_NOTICE)
            .temporaryC2Document(createC2DocumentBundle(c2Supplement, c2SupportingDocument))
            .temporaryOtherApplicationsBundle(createOtherApplicationsBundle(otherSupplement, otherSupportingDocument))
            .temporaryPbaPayment(pbaPayment)
            .applicantsList(applicantsList)
            .respondents1(respondentsInCase)
            .build();

        assertThat(caseData.getAllRespondents()).isEqualTo(respondentsInCase);

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getAuthor()).isEqualTo(HMCTS);
        assertThat(actual.getPbaPayment()).isEqualTo(pbaPayment);
        assertThat(actual.getC2DocumentBundle().getApplicantName()).isEqualTo(APPLICANT_NAME);
        assertThat(actual.getOtherApplicationsBundle().getApplicantName()).isEqualTo(APPLICANT_NAME);

        assertC2DocumentBundle(actual.getC2DocumentBundle(), c2Supplement, c2SupportingDocument);
        assertOtherDocumentBundle(actual.getOtherApplicationsBundle(), otherSupplement, otherSupportingDocument);
    }

    @Test
    void shouldBuildBundleWhenLAUploadConfidentialC2Application() {
        Supplement c2Supplement = createSupplementsBundle();
        SupportingEvidenceBundle c2SupportingDocument = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0))
            .listItems(DYNAMIC_LIST_ELEMENTS).build();

        List<Element<Respondent>> respondentsInCase = wrapElements(
            Respondent.builder().party(
                RespondentParty.builder().firstName("First").lastName("Respondent")
                    .address(Address.builder().postcode("SE1").build()).build()).build());

        CaseData caseData = CaseData.builder().temporaryPbaPayment(pbaPayment)
            .additionalApplicationType(List.of(C2_ORDER))
            .c2Type(WITH_NOTICE)
            .isC2Confidential(YesNo.YES)
            .temporaryC2Document(createC2DocumentBundle(c2Supplement, c2SupportingDocument))
            .temporaryPbaPayment(pbaPayment)
            .applicantsList(applicantsList)
            .respondents1(respondentsInCase)
            .localAuthorityPolicy(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(CaseRole.LASOLICITOR.formattedName())
                .build())
            .build();

        given(user.getCaseRoles(any())).willReturn(Set.of(CaseRole.LASOLICITOR));

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getPbaPayment()).isEqualTo(pbaPayment);
        assertThat(actual.getC2DocumentBundle()).isNull();
        assertThat(actual.getC2DocumentBundleConfidential().getApplicantName()).isEqualTo(APPLICANT_NAME);
        assertThat(actual.getC2DocumentBundleLA().getApplicantName()).isEqualTo(APPLICANT_NAME);

        assertC2DocumentBundle(actual.getC2DocumentBundleConfidential(), c2Supplement, c2SupportingDocument);
        assertC2DocumentBundle(actual.getC2DocumentBundleLA(), c2Supplement, c2SupportingDocument);
    }

    @Test
    void shouldBuildBundleWhenSolicitorUploadConfidentialC2Application() {
        Supplement c2Supplement = createSupplementsBundle();
        SupportingEvidenceBundle c2SupportingDocument = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0))
            .listItems(DYNAMIC_LIST_ELEMENTS).build();

        List<Element<Respondent>> respondentsInCase = wrapElements(
            Respondent.builder().party(
                RespondentParty.builder().firstName("First").lastName("Respondent")
                    .address(Address.builder().postcode("SE1").build()).build()).build());

        CaseData caseData = CaseData.builder().temporaryPbaPayment(pbaPayment)
            .additionalApplicationType(List.of(C2_ORDER))
            .c2Type(WITH_NOTICE)
            .isC2Confidential(YesNo.YES)
            .temporaryC2Document(createC2DocumentBundle(c2Supplement, c2SupportingDocument))
            .temporaryPbaPayment(pbaPayment)
            .applicantsList(applicantsList)
            .respondents1(respondentsInCase)
            .respondentPolicyData(RespondentPolicyData.builder()
                .respondentPolicy0(OrganisationPolicy.builder()
                    .orgPolicyCaseAssignedRole(CaseRole.SOLICITORA.formattedName()).build())
                .build())
            .build();

        given(user.getCaseRoles(any())).willReturn(Set.of(CaseRole.SOLICITORA));

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getPbaPayment()).isEqualTo(pbaPayment);
        assertThat(actual.getC2DocumentBundle()).isNull();
        assertThat(actual.getC2DocumentBundleConfidential().getApplicantName()).isEqualTo(APPLICANT_NAME);
        assertThat(actual.getC2DocumentBundleResp0().getApplicantName()).isEqualTo(APPLICANT_NAME);

        assertC2DocumentBundle(actual.getC2DocumentBundleConfidential(), c2Supplement, c2SupportingDocument);
        assertC2DocumentBundle(actual.getC2DocumentBundleResp0(), c2Supplement, c2SupportingDocument);
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
            Arguments.of(AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(
                        C2DocumentBundle.builder()
                            .type(WITHOUT_NOTICE)
                            .document(DocumentReference.builder().build())
                            .build())
                    .build(),
                List.of(C2_APPLICATION)),
            Arguments.of(AdditionalApplicationsBundle.builder()
                    .c2DocumentBundleConfidential(
                        C2DocumentBundle.builder()
                            .type(WITHOUT_NOTICE)
                            .document(DocumentReference.builder().build())
                            .build())
                    .build(),
                List.of(C2_APPLICATION)),
            Arguments.of(AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(
                        OtherApplicationsBundle.builder()
                            .applicationType(C1_PARENTAL_RESPONSIBILITY)
                            .document(DocumentReference.builder().build())
                            .build())
                    .build(),
                List.of(ApplicationType.C1_PARENTAL_RESPONSIBILITY)),
            Arguments.of(AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(
                        C2DocumentBundle.builder()
                            .type(WITH_NOTICE)
                            .document(DocumentReference.builder().build())
                            .build())
                    .otherApplicationsBundle(
                        OtherApplicationsBundle.builder()
                            .applicationType(C1_PARENTAL_RESPONSIBILITY)
                            .document(DocumentReference.builder().build())
                            .build())
                    .build(),
                List.of(C2_APPLICATION, ApplicationType.C1_PARENTAL_RESPONSIBILITY))
        );
    }

    @Nested
    class SkipPayments {

        @Test
        void shouldSkipPaymentsWhenAllConditionsValid() {
            HearingBooking booking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(15))
                .build();

            CaseData caseData = CaseData.builder()
                .additionalApplicationType(List.of(C2_ORDER))
                .temporaryC2Document(C2DocumentBundle.builder()
                    .c2AdditionalOrdersRequested(List.of(REQUESTING_ADJOURNMENT))
                    .build())
                .build();

            boolean result = underTest.shouldSkipPayments(caseData, booking, caseData.getTemporaryC2Document());
            assertThat(result).isTrue();
        }

        @Test
        void shouldNotSkipPaymentsWhenLessThan14DaysBeforeHearing() {
            HearingBooking booking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(13))
                .build();

            CaseData caseData = CaseData.builder()
                .additionalApplicationType(List.of(C2_ORDER))
                .temporaryC2Document(C2DocumentBundle.builder()
                    .c2AdditionalOrdersRequested(List.of(REQUESTING_ADJOURNMENT))
                    .build())
                .build();

            boolean result = underTest.shouldSkipPayments(caseData, booking, caseData.getTemporaryC2Document());
            assertThat(result).isFalse();
        }

        @Test
        void shouldNotSkipPaymentsWhenOtherOrderAppliedFor() {
            HearingBooking booking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(15))
                .build();

            CaseData caseData = CaseData.builder()
                .additionalApplicationType(List.of(C2_ORDER, OTHER_ORDER))
                .temporaryC2Document(C2DocumentBundle.builder()
                    .c2AdditionalOrdersRequested(List.of(REQUESTING_ADJOURNMENT))
                    .build())
                .build();

            boolean result = underTest.shouldSkipPayments(caseData, booking, caseData.getTemporaryC2Document());
            assertThat(result).isFalse();
        }

        @Test
        void shouldNotSkipPaymentsWhenAnotherC2AdditionalOrderRequested() {
            HearingBooking booking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(15))
                .build();

            CaseData caseData = CaseData.builder()
                .additionalApplicationType(List.of(C2_ORDER))
                .temporaryC2Document(C2DocumentBundle.builder()
                    .c2AdditionalOrdersRequested(List.of(REQUESTING_ADJOURNMENT, CHANGE_SURNAME_OR_REMOVE_JURISDICTION))
                    .build())
                .build();

            boolean result = underTest.shouldSkipPayments(caseData, booking, caseData.getTemporaryC2Document());
            assertThat(result).isFalse();
        }

        @Test
        void shouldNotSkipPaymentsWhenNotRequestingAdjournment() {
            HearingBooking booking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(15))
                .build();

            CaseData caseData = CaseData.builder()
                .additionalApplicationType(List.of(C2_ORDER))
                .temporaryC2Document(C2DocumentBundle.builder()
                    .c2AdditionalOrdersRequested(List.of(CHANGE_SURNAME_OR_REMOVE_JURISDICTION))
                    .build())
                .build();

            boolean result = underTest.shouldSkipPayments(caseData, booking, caseData.getTemporaryC2Document());
            assertThat(result).isFalse();
        }

        @Test
        void shouldNotSkipPaymentsWhenApplyingForAnOtherOrder() {
            HearingBooking booking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(15))
                .build();

            CaseData caseData = CaseData.builder()
                .additionalApplicationType(List.of(OTHER_ORDER))
                .temporaryC2Document(C2DocumentBundle.builder()
                    .c2AdditionalOrdersRequested(List.of(CHANGE_SURNAME_OR_REMOVE_JURISDICTION))
                    .build())
                .build();

            boolean result = underTest.shouldSkipPayments(caseData, booking, caseData.getTemporaryC2Document());
            assertThat(result).isFalse();
        }
    }

    @Nested
    class PostSubmitProcessing {
        private static final CaseData CASE_DATA = CaseData.builder().court(COURT_1).build();

        @Test
        void shouldSetSupplementsToEmptyListIfNonePresent() {
            C2DocumentBundle bundle = C2DocumentBundle.builder()
                .document(DOCUMENT)
                .supplementsBundle(List.of())
                .build();
            C2DocumentBundle converted = underTest.convertC2Bundle(bundle, CASE_DATA);

            assertThat(converted.getSupplementsBundle()).isEmpty();
            assertThat(converted.getSupplementsBundle()).isNotNull();
        }

        @Test
        void shouldSealC2Document() {
            C2DocumentBundle bundle = C2DocumentBundle.builder()
                .document(DOCUMENT)
                .supplementsBundle(wrapElementsWithRandomUUID(Supplement.builder()
                    .document(SUPPLEMENT_DOCUMENT)
                    .build()))
                .build();
            C2DocumentBundle converted = underTest.convertC2Bundle(bundle, CASE_DATA);

            assertThat(converted.getDocument())
                .isEqualTo(SEALED_DOCUMENT);
            assertThat(converted.getSupplementsBundle().get(0).getValue().getDocument())
                .isEqualTo(SEALED_SUPPLEMENT_DOCUMENT);
        }

        @Test
        void shouldSealOtherDocument() {
            OtherApplicationsBundle bundle = OtherApplicationsBundle.builder()
                .document(DOCUMENT)
                .supplementsBundle(wrapElementsWithRandomUUID(Supplement.builder()
                    .document(SUPPLEMENT_DOCUMENT)
                    .build()))
                .build();
            OtherApplicationsBundle converted = underTest.convertOtherBundle(bundle, CASE_DATA);

            assertThat(converted.getDocument())
                .isEqualTo(SEALED_DOCUMENT);
            assertThat(converted.getSupplementsBundle().get(0).getValue().getDocument())
                .isEqualTo(SEALED_SUPPLEMENT_DOCUMENT);
        }

        @Test
        void shouldSealConfidentialC2Document() {
            C2DocumentBundle bundle = C2DocumentBundle.builder()
                .document(DOCUMENT)
                .supplementsBundle(wrapElementsWithRandomUUID(Supplement.builder()
                    .document(SUPPLEMENT_DOCUMENT)
                    .build()))
                .build();

            CaseData caseData = CASE_DATA.toBuilder()
                .isC2Confidential(YesNo.YES)
                .respondentPolicyData(RespondentPolicyData.builder()
                    .respondentPolicy0(OrganisationPolicy.builder()
                        .orgPolicyCaseAssignedRole(CaseRole.SOLICITORA.formattedName()).build())
                    .build())
                .build();

            AdditionalApplicationsBundle.AdditionalApplicationsBundleBuilder builder =
                AdditionalApplicationsBundle.builder();

            given(user.getCaseRoles(any())).willReturn(Set.of(CaseRole.SOLICITORA));
            underTest.convertConfidentialC2Bundle(caseData, bundle, builder);

            AdditionalApplicationsBundle additionalApplicationsBundle = builder.build();

            assertThat(additionalApplicationsBundle.getC2DocumentBundleConfidential()
                .getDocument())
                .isEqualTo(SEALED_DOCUMENT);
            assertThat(additionalApplicationsBundle.getC2DocumentBundleConfidential()
                .getSupplementsBundle().get(0).getValue().getDocument())
                .isEqualTo(SEALED_SUPPLEMENT_DOCUMENT);

            assertThat(additionalApplicationsBundle.getC2DocumentBundleResp0()
                .getDocument())
                .isEqualTo(SEALED_DOCUMENT);
            assertThat(additionalApplicationsBundle.getC2DocumentBundleResp0()
                .getSupplementsBundle().get(0).getValue().getDocument())
                .isEqualTo(SEALED_SUPPLEMENT_DOCUMENT);
        }
    }

    private void assertC2DocumentBundle(C2DocumentBundle actualC2Bundle, Supplement expectedSupplement,
                                        SupportingEvidenceBundle expectedSupportingEvidence) {
        assertThat(actualC2Bundle.getId()).isNotNull();
        assertThat(actualC2Bundle.getDocument().getFilename()).isEqualTo(DOCUMENT.getFilename());
        assertThat(actualC2Bundle.getType()).isEqualTo(WITH_NOTICE);
        assertThat(actualC2Bundle.getSupportingEvidenceBundle()).hasSize(1);
        assertThat(actualC2Bundle.getSupplementsBundle()).hasSize(1);

        assertSupplementsBundle(actualC2Bundle.getSupplementsBundle().get(0).getValue(), expectedSupplement);
        assertSupportingEvidenceBundle(
            actualC2Bundle.getSupportingEvidenceBundle().get(0).getValue(), expectedSupportingEvidence
        );
    }

    private void assertOtherDocumentBundle(OtherApplicationsBundle actual, Supplement expectedSupplement,
                                           SupportingEvidenceBundle expectedSupportingDocument) {
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getDocument().getFilename()).isEqualTo(DOCUMENT.getFilename());
        assertThat(actual.getApplicationType()).isEqualTo(C1_PARENTAL_RESPONSIBILITY);
        assertThat(actual.getParentalResponsibilityType()).isEqualTo(PR_BY_FATHER);
        assertThat(actual.getAuthor()).isEqualTo(HMCTS);
        assertThat(actual.getSupportingEvidenceBundle()).hasSize(1);
        assertThat(actual.getSupplementsBundle()).hasSize(1);

        assertSupplementsBundle(actual.getSupplementsBundle().get(0).getValue(), expectedSupplement);
        assertSupportingEvidenceBundle(
            actual.getSupportingEvidenceBundle().get(0).getValue(), expectedSupportingDocument
        );
    }

    private void assertSupplementsBundle(Supplement actual, Supplement exampleOfExpectedSupplement) {
        Supplement expectedSupplement = exampleOfExpectedSupplement.toBuilder()
            .dateTimeUploaded(time.now())
            .uploadedBy(HMCTS)
            .document(SUPPLEMENT_DOCUMENT)
            .build();

        assertThat(actual).isEqualTo(expectedSupplement);
    }

    private void assertSupportingEvidenceBundle(SupportingEvidenceBundle actual, SupportingEvidenceBundle expected) {
        SupportingEvidenceBundle expectedBundle = expected.toBuilder()
            .dateTimeUploaded(time.now())
            .uploadedBy(HMCTS)
            .build();

        assertThat(actual).isEqualTo(expectedBundle);
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

    private C2DocumentBundle createC2DocumentBundle(Supplement supplementsBundle,
                                                    SupportingEvidenceBundle supportingEvidenceBundle) {
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
