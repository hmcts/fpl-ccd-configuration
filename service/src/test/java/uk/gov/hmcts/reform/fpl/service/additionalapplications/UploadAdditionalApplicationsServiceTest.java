package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.ApplicationPermissionType;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.enums.SupplementType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentPolicyData;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC2OrderDocument;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.C2AdditionalApplicationEventData;
import uk.gov.hmcts.reform.fpl.model.event.UploadAdditionalApplicationsEventData;
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.PbaService;
import uk.gov.hmcts.reform.fpl.service.PeopleInCaseService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_1;
import static uk.gov.hmcts.reform.fpl.Constants.USER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.Constants.USER_ID;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.C2_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.OTHER_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested.CHANGE_SURNAME_OR_REMOVE_JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationRouteType.APPLY_ONLINE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationRouteType.PAPER_FORM;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_PARENTAL_RESPONSIBILITY;
import static uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType.PR_BY_FATHER;
import static uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType.WALES;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C20_SECURE_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithRandomUUID;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentWithName;

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

    private static final DocumentReference SUPPLEMENT_DOCUMENT = testDocumentReference("SupplementFile.doc");
    private static final DocumentReference SEALED_SUPPLEMENT_DOCUMENT =
        testDocumentReference("Sealed_SupplementFile.pdf");
    private static final DocumentReference  SEALED_DOCUMENT = testDocumentReference("Sealed_TestDocument.pdf");

    private static final DocumentReference SUPPORTING_DOCUMENT = testDocumentReference("SupportingEvidenceFile.doc");

    private static final DocmosisDocument C2_ONLINE_DOCMOSIS_DOCUMENT =
        testDocmosisDocument(TestDataHelper.DOCUMENT_CONTENT);
    private static final String C2_APPLICATION_NAME = "C2_application.pdf";
    private static final Document C2_ONLINE_DOCUMENT = testDocumentWithName(C2_APPLICATION_NAME);

    private final RequestData requestData = mock(RequestData.class);
    private final Time time = new FixedTimeConfiguration().stoppedTime();
    private final IdamClient idamClient = mock(IdamClient.class);
    private final UserService user = mock(UserService.class);
    private final ManageDocumentService manageDocumentService = mock(ManageDocumentService.class);
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService =
        mock(DocmosisDocumentGeneratorService.class);
    private final DocumentUploadHelper uploadHelper = mock(DocumentUploadHelper.class);
    private final PeopleInCaseService peopleInCaseService = mock(PeopleInCaseService.class);
    private final DocumentSealingService documentSealingService = mock(DocumentSealingService.class);
    private final UploadDocumentService uploadDocumentService = mock(UploadDocumentService.class);
    private final PbaService pbaService = mock(PbaService.class);
    private final JudicialService judicialService = mock(JudicialService.class);

    private UploadAdditionalApplicationsService underTest;

    @BeforeEach()
    void init() {

        given(idamClient.getUserDetails(USER_AUTH_TOKEN)).willReturn(createUserDetailsWithHmctsRole());
        given(requestData.authorisation()).willReturn(USER_AUTH_TOKEN);
        given(documentSealingService.sealDocument(SUPPLEMENT_DOCUMENT, COURT_1, SealType.ENGLISH))
            .willReturn(SEALED_SUPPLEMENT_DOCUMENT);
        given(documentSealingService.sealDocument(DOCUMENT, COURT_1, SealType.ENGLISH))
            .willReturn(SEALED_DOCUMENT);
        underTest = new UploadAdditionalApplicationsService(time, user, manageDocumentService,
            docmosisDocumentGeneratorService, uploadHelper, documentSealingService, uploadDocumentService,
            pbaService, judicialService);
        given(user.isHmctsUser()).willReturn(true);
        given(manageDocumentService.getUploaderType(any())).willReturn(DocumentUploaderType.HMCTS);
        given(uploadHelper.getUploadedDocumentUserDetails()).willReturn(HMCTS);
        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisC2OrderDocument.class),
            any(), any(), any())).willReturn(C2_ONLINE_DOCMOSIS_DOCUMENT);
        given(uploadDocumentService.uploadPDF(C2_ONLINE_DOCMOSIS_DOCUMENT.getBytes(), C2_APPLICATION_NAME))
            .willReturn(C2_ONLINE_DOCUMENT);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldBuildExpectedPaperC2DocumentBundle() {
        Supplement supplement = createSupplementsBundle();
        SupportingEvidenceBundle supportingEvidenceBundle = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();
        PBAPayment expectedPbaPayment = PBAPayment.builder().pbaNumber("PBA12345").usePbaPayment("Yes").build();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0))
            .listItems(DYNAMIC_LIST_ELEMENTS)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                .additionalApplicationType(List.of(C2_ORDER))
                .temporaryC2Document(createC2EventData(supplement, supportingEvidenceBundle))
                .temporaryPbaPayment(pbaPayment)
                .applicantsList(applicantsList)
                .c2Type(WITH_NOTICE)
                .c2ApplicationRoute(PAPER_FORM)
                .build())
            .build();

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getAuthor()).isEqualTo(HMCTS);
        assertThat(actual.getPbaPayment()).isEqualTo(expectedPbaPayment);
        assertThat(actual.getC2DocumentBundle().getApplicantName()).isEqualTo(APPLICANT_NAME);
        assertThat(actual.getApplicationReviewed()).isEqualTo(YesNo.NO);

        assertPaperC2DocumentBundle(actual.getC2DocumentBundle(), supplement, createSupportingEvidenceBundleBuilder()
            .uploaderType(DocumentUploaderType.HMCTS)
            .uploaderCaseRoles(List.of())
            .build());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldBuildExpectedOnlineC2DocumentBundle() {
        Supplement supplement = createSupplementsBundle();
        SupportingEvidenceBundle supportingEvidenceBundle = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();
        PBAPayment expectedPbaPayment = PBAPayment.builder().pbaNumber("PBA12345").usePbaPayment("Yes").build();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0))
            .listItems(DYNAMIC_LIST_ELEMENTS)
            .build();

        CaseData caseData = CaseData.builder()
            .court(COURT_1)
            .familyManCaseNumber("12345")
            .amountToPay("9000")
            .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                .additionalApplicationType(List.of(C2_ORDER))
                .temporaryC2Document(createC2EventDataForOnlineForm(supplement, supportingEvidenceBundle))
                .temporaryPbaPayment(pbaPayment)
                .applicantsList(applicantsList)
                .c2Type(WITH_NOTICE)
                .isC2Confidential(YesNo.NO)
                .c2ApplicationRoute(APPLY_ONLINE)
                .build())
            .build();

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getAuthor()).isEqualTo(HMCTS);
        assertThat(actual.getPbaPayment()).isEqualTo(expectedPbaPayment);
        assertThat(actual.getC2DocumentBundle().getApplicantName()).isEqualTo(APPLICANT_NAME);
        assertThat(actual.getApplicationReviewed()).isEqualTo(YesNo.NO);

        assertOnlineC2DocumentBundle(actual.getC2DocumentBundle(), supplement, createSupportingEvidenceBundleBuilder()
            .uploaderType(DocumentUploaderType.HMCTS)
            .uploaderCaseRoles(List.of())
            .build());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotConvertApplications(boolean isHmctsUser) {
        given(user.isHmctsUser()).willReturn(isHmctsUser);
        given(uploadHelper.getUploadedDocumentUserDetails()).willReturn(USER_EMAIL);

        Supplement supplement = createSupplementsBundle();
        SupportingEvidenceBundle supportingEvidenceBundle = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0))
            .listItems(DYNAMIC_LIST_ELEMENTS)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                .additionalApplicationType(List.of(C2_ORDER))
                .temporaryC2Document(createC2EventData(supplement, supportingEvidenceBundle))
                .temporaryPbaPayment(pbaPayment)
                .applicantsList(applicantsList)
                .c2Type(WITH_NOTICE)
                .c2ApplicationRoute(PAPER_FORM)
                .build())
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
    @SuppressWarnings("unchecked")
    void shouldBuildOtherApplicationsBundleWithOtherApplicantName() {
        Supplement supplement = createSupplementsBundle();
        SupportingEvidenceBundle supportingDocument = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();
        PBAPayment expectedPbaPayment = PBAPayment.builder().pbaNumber("PBA12345").usePbaPayment("Yes").build();

        // select "Someone else"
        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(1))
            .listItems(DYNAMIC_LIST_ELEMENTS)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                .additionalApplicationType(List.of(OTHER_ORDER))
                .temporaryOtherApplicationsBundle(createOtherApplicationsBundle(supplement, supportingDocument))
                .temporaryPbaPayment(pbaPayment)
                .applicantsList(applicantsList)
                .otherApplicant("some other name")
                .build())
            .build();

        given(peopleInCaseService.getSelectedRespondents(any())).willReturn(List.of());
        given(peopleInCaseService.getPeopleNotified(any(), eq(List.of()), eq(List.of()))).willReturn("");

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getAuthor()).isEqualTo(HMCTS);
        assertThat(actual.getPbaPayment()).isEqualTo(expectedPbaPayment);
        assertThat(actual.getOtherApplicationsBundle().getApplicantName()).isEqualTo("some other name");
        assertThat(actual.getOtherApplicationsBundle().getRespondents()).isEmpty();

        assertOtherDocumentBundle(actual.getOtherApplicationsBundle(), supplement,
            createSupportingEvidenceBundleBuilder()
                .uploaderType(DocumentUploaderType.HMCTS)
                .uploaderCaseRoles(List.of())
                .build());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowIllegalArgumentExceptionWhenApplicantIsNullOrEmpty(String otherApplicantName) {
        // select applicant "Someone else"
        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(1)).listItems(DYNAMIC_LIST_ELEMENTS).build();

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                .additionalApplicationType(List.of(OTHER_ORDER))
                .applicantsList(applicantsList)
                .otherApplicant(otherApplicantName)
                .build())
            .build();

        assertThatThrownBy(() -> underTest.buildAdditionalApplicationsBundle(caseData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Applicant should not be empty");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenApplicantListIsEmpty() {
        DynamicList applicantsList = DynamicList.builder().build();

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                .additionalApplicationType(List.of(OTHER_ORDER))
                .applicantsList(applicantsList)
                .build())
            .build();

        assertThatThrownBy(() -> underTest.buildAdditionalApplicationsBundle(caseData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Applicant should not be empty");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldBuildAdditionalApplicationsBundleWithC2ApplicationAndOtherApplicationsBundles() {
        Supplement c2Supplement = createSupplementsBundle();
        SupportingEvidenceBundle c2SupportingDocument = createSupportingEvidenceBundle();

        Supplement otherSupplement = createSupplementsBundle(C20_SECURE_ACCOMMODATION);
        SupportingEvidenceBundle otherSupportingDocument = createSupportingEvidenceBundle("other document");
        PBAPayment pbaPayment = buildPBAPayment();
        PBAPayment expectedPbaPayment = PBAPayment.builder().pbaNumber("PBA12345").usePbaPayment("Yes").build();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0))
            .listItems(DYNAMIC_LIST_ELEMENTS).build();

        List<Element<Respondent>> respondentsInCase = wrapElements(
            Respondent.builder().party(
                RespondentParty.builder().firstName("First").lastName("Respondent")
                    .address(Address.builder().postcode("SE1").build()).build()).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                .temporaryPbaPayment(pbaPayment)
                .additionalApplicationType(List.of(C2_ORDER, OTHER_ORDER))
                .c2Type(WITH_NOTICE)
                .temporaryC2Document(createC2EventData(c2Supplement, c2SupportingDocument))
                .temporaryOtherApplicationsBundle(createOtherApplicationsBundle(otherSupplement,
                    otherSupportingDocument))
                .temporaryPbaPayment(pbaPayment)
                .applicantsList(applicantsList)
                .c2ApplicationRoute(PAPER_FORM)
                .build())
            .respondents1(respondentsInCase)
            .build();

        assertThat(caseData.getAllRespondents()).isEqualTo(respondentsInCase);

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getAuthor()).isEqualTo(HMCTS);
        assertThat(actual.getPbaPayment()).isEqualTo(expectedPbaPayment);
        assertThat(actual.getC2DocumentBundle().getApplicantName()).isEqualTo(APPLICANT_NAME);
        assertThat(actual.getOtherApplicationsBundle().getApplicantName()).isEqualTo(APPLICANT_NAME);

        assertPaperC2DocumentBundle(actual.getC2DocumentBundle(), c2Supplement, createSupportingEvidenceBundleBuilder()
            .uploaderType(DocumentUploaderType.HMCTS)
            .uploaderCaseRoles(List.of()).build());
        assertOtherDocumentBundle(actual.getOtherApplicationsBundle(), otherSupplement,
            createSupportingEvidenceBundleBuilder("other document")
                .uploaderCaseRoles(List.of())
                .uploaderType(DocumentUploaderType.HMCTS)
                .build());
    }

    @Test
    void shouldBuildPaperBundleWhenLAUploadConfidentialC2Application() {
        Supplement c2Supplement = createSupplementsBundle();
        SupportingEvidenceBundle c2SupportingDocument = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();
        PBAPayment expectedPbaPayment = PBAPayment.builder().pbaNumber("PBA12345").usePbaPayment("Yes").build();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0))
            .listItems(DYNAMIC_LIST_ELEMENTS).build();

        List<Element<Respondent>> respondentsInCase = wrapElements(
            Respondent.builder().party(
                RespondentParty.builder().firstName("First").lastName("Respondent")
                    .address(Address.builder().postcode("SE1").build()).build()).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                .temporaryPbaPayment(pbaPayment)
                .additionalApplicationType(List.of(C2_ORDER))
                .c2Type(WITH_NOTICE)
                .isC2Confidential(YesNo.YES)
                .temporaryC2Document(createC2EventData(c2Supplement, c2SupportingDocument))
                .temporaryPbaPayment(pbaPayment)
                .applicantsList(applicantsList)
                .c2ApplicationRoute(PAPER_FORM)
                .build())
            .respondents1(respondentsInCase)
            .localAuthorityPolicy(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(CaseRole.LASOLICITOR.formattedName())
                .build())
            .build();

        given(user.getCaseRoles(any())).willReturn(Set.of(CaseRole.LASOLICITOR));

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getPbaPayment()).isEqualTo(expectedPbaPayment);
        assertThat(actual.getC2DocumentBundle()).isNull();
        assertThat(actual.getC2DocumentBundleConfidential().getApplicantName()).isEqualTo(APPLICANT_NAME);
        assertThat(actual.getC2DocumentBundleLA().getApplicantName()).isEqualTo(APPLICANT_NAME);

        assertPaperC2DocumentBundle(actual.getC2DocumentBundleConfidential(), c2Supplement, c2SupportingDocument);
        assertPaperC2DocumentBundle(actual.getC2DocumentBundleLA(), c2Supplement, c2SupportingDocument);
    }

    @Test
    void shouldBuildPaperBundleWhenSolicitorUploadConfidentialC2Application() {
        Supplement c2Supplement = createSupplementsBundle();
        SupportingEvidenceBundle c2SupportingDocument = createSupportingEvidenceBundle();
        PBAPayment pbaPayment = buildPBAPayment();
        PBAPayment expectedPbaPayment = PBAPayment.builder().pbaNumber("PBA12345").usePbaPayment("Yes").build();

        DynamicList applicantsList = DynamicList.builder()
            .value(DYNAMIC_LIST_ELEMENTS.get(0))
            .listItems(DYNAMIC_LIST_ELEMENTS).build();

        List<Element<Respondent>> respondentsInCase = wrapElements(
            Respondent.builder().party(
                RespondentParty.builder().firstName("First").lastName("Respondent")
                    .address(Address.builder().postcode("SE1").build()).build()).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                .temporaryPbaPayment(pbaPayment)
                .additionalApplicationType(List.of(C2_ORDER))
                .c2Type(WITH_NOTICE)
                .isC2Confidential(YesNo.YES)
                .temporaryC2Document(createC2EventData(c2Supplement, c2SupportingDocument))
                .temporaryPbaPayment(pbaPayment)
                .applicantsList(applicantsList)
                .c2ApplicationRoute(PAPER_FORM)
                .build())
            .respondents1(respondentsInCase)
            .respondentPolicyData(RespondentPolicyData.builder()
                .respondentPolicy0(OrganisationPolicy.builder()
                    .orgPolicyCaseAssignedRole(CaseRole.SOLICITORA.formattedName()).build())
                .build())
            .build();

        given(user.getCaseRoles(any())).willReturn(Set.of(CaseRole.SOLICITORA));

        AdditionalApplicationsBundle actual = underTest.buildAdditionalApplicationsBundle(caseData);

        assertThat(actual.getPbaPayment()).isEqualTo(expectedPbaPayment);
        assertThat(actual.getC2DocumentBundle()).isNull();
        assertThat(actual.getC2DocumentBundleConfidential().getApplicantName()).isEqualTo(APPLICANT_NAME);
        assertThat(actual.getC2DocumentBundleResp0().getApplicantName()).isEqualTo(APPLICANT_NAME);

        assertPaperC2DocumentBundle(actual.getC2DocumentBundleConfidential(), c2Supplement, c2SupportingDocument);
        assertPaperC2DocumentBundle(actual.getC2DocumentBundleResp0(), c2Supplement, c2SupportingDocument);
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

    @Test
    void shouldGiveRoleTypeForAllocatedJudge() {
        Judge allocatedJudge = Judge.builder()
            .judgeJudicialUser(JudicialUser.builder()
                .idamId("1234")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .allocatedJudge(allocatedJudge)
            .build();

        when(judicialService.getAllocatedJudge(caseData)).thenReturn(Optional.of(allocatedJudge));
        when(judicialService.getAllocatedJudgeAndLegalAdvisorRoleAssignments(eq(caseData.getId())))
            .thenReturn(List.of(RoleAssignment.builder().roleName("allocated-judge").build()));

        assertThat(underTest.getAllocatedJudgeOrLegalAdviserType(caseData))
            .isEqualTo(JudicialMessageRoleType.ALLOCATED_JUDGE);
    }

    @Test
    void shouldGiveRoleTypeForAllocatedLegalAdvisor() {
        Judge allocatedLegalAdviser = Judge.builder()
            .judgeJudicialUser(JudicialUser.builder()
                .idamId("1234")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .allocatedJudge(allocatedLegalAdviser)
            .build();

        when(judicialService.getAllocatedJudge(caseData)).thenReturn(Optional.of(allocatedLegalAdviser));
        when(judicialService.getAllocatedJudgeAndLegalAdvisorRoleAssignments(eq(caseData.getId())))
            .thenReturn(List.of(RoleAssignment.builder().roleName("allocated-legal-adviser").build()));

        assertThat(underTest.getAllocatedJudgeOrLegalAdviserType(caseData))
            .isEqualTo(JudicialMessageRoleType.OTHER);
    }

    @Test
    void shouldReturnGenericTaskForAllocatedJudgeOrLegalAdvisorWithWrongAmRole() {
        Judge allocatedJudge = Judge.builder()
            .judgeJudicialUser(JudicialUser.builder()
                .idamId("1234")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .allocatedJudge(allocatedJudge)
            .id(1234L)
            .build();

        when(judicialService.getAllocatedJudge(caseData)).thenReturn(Optional.of(allocatedJudge));
        when(judicialService.getAllocatedJudgeAndLegalAdvisorRoleAssignments(eq(caseData.getId())))
            .thenReturn(List.of(RoleAssignment.builder().roleName("not-a-judge").build()));

        assertThat(underTest.getAllocatedJudgeOrLegalAdviserType(caseData))
            .isEqualTo(JudicialMessageRoleType.CTSC);
    }

    @Test
    void shouldReturnGenericTaskForAllocatedJudgeOrLegalAdvisorWhenInvalidJrdUser() {
        Judge allocatedJudge = Judge.builder()
            .judgeJudicialUser(JudicialUser.builder()
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .allocatedJudge(allocatedJudge)
            .id(1234L)
            .build();

        when(judicialService.getAllocatedJudge(caseData)).thenReturn(Optional.of(allocatedJudge));

        assertThat(underTest.getAllocatedJudgeOrLegalAdviserType(caseData))
            .isEqualTo(JudicialMessageRoleType.CTSC);
    }

    @Test
    void shouldReturnStandardCtscRoleWhenAllocatedHasNoJudicialUserProfile() {
        Judge allocatedJudge = Judge.builder()
            .judgeTitle(JudgeOrMagistrateTitle.MAGISTRATES)
            .judgeJudicialUser(JudicialUser.builder()
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .allocatedJudge(allocatedJudge)
            .id(1234L)
            .build();

        assertThat(underTest.getAllocatedJudgeOrLegalAdviserType(caseData))
            .isEqualTo(JudicialMessageRoleType.CTSC);
    }

    @Test
    void shouldReturnStandardCtscRoleWhenNoAllocatedJudgeOrLegalAdvisor() {
        CaseData caseData = CaseData.builder()
            .id(1234L)
            .build();

        assertThat(underTest.getAllocatedJudgeOrLegalAdviserType(caseData))
            .isEqualTo(JudicialMessageRoleType.CTSC);
    }

    @Nested
    class SkipPayments {

        @Test
        void shouldSkipPaymentsWhenAllConditionsValid() {
            HearingBooking booking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(15))
                .build();

            CaseData caseData = CaseData.builder()
                .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                    .additionalApplicationType(List.of(C2_ORDER))
                    .temporaryC2Document(C2AdditionalApplicationEventData.builder()
                        .isHearingAdjournmentRequired(YesNo.YES)
                        .build())
                    .build())
                .build();

            boolean result = underTest.shouldSkipPayments(caseData, booking,
                caseData.getUploadAdditionalApplicationsEventData().getTemporaryC2Document());
            assertThat(result).isTrue();
        }

        @Test
        void shouldNotSkipPaymentsWhenLessThan14DaysBeforeHearing() {
            HearingBooking booking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(13))
                .build();

            CaseData caseData = CaseData.builder()
                .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                    .additionalApplicationType(List.of(C2_ORDER))
                    .temporaryC2Document(C2AdditionalApplicationEventData.builder()
                        .isHearingAdjournmentRequired(YesNo.YES)
                        .build())
                    .build())
                .build();

            boolean result = underTest.shouldSkipPayments(caseData, booking,
                caseData.getUploadAdditionalApplicationsEventData().getTemporaryC2Document());
            assertThat(result).isFalse();
        }

        @Test
        void shouldNotSkipPaymentsWhenOtherOrderAppliedFor() {
            HearingBooking booking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(15))
                .build();

            CaseData caseData = CaseData.builder()
                .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                    .additionalApplicationType(List.of(C2_ORDER, OTHER_ORDER))
                    .temporaryC2Document(C2AdditionalApplicationEventData.builder()
                        .isHearingAdjournmentRequired(YesNo.YES)
                        .build())
                    .build())
                .build();

            boolean result = underTest.shouldSkipPayments(caseData, booking,
                caseData.getUploadAdditionalApplicationsEventData().getTemporaryC2Document());
            assertThat(result).isFalse();
        }

        @Test
        void shouldNotSkipPaymentsWhenAnotherC2AdditionalOrderRequested() {
            HearingBooking booking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(15))
                .build();

            CaseData caseData = CaseData.builder()
                .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                    .additionalApplicationType(List.of(C2_ORDER))
                    .temporaryC2Document(C2AdditionalApplicationEventData.builder()
                        .isHearingAdjournmentRequired(YesNo.YES)
                        .c2AdditionalOrdersRequested(List.of(REQUESTING_ADJOURNMENT,
                            CHANGE_SURNAME_OR_REMOVE_JURISDICTION))
                        .build())
                    .build())
                .build();

            boolean result = underTest.shouldSkipPayments(caseData, booking,
                caseData.getUploadAdditionalApplicationsEventData().getTemporaryC2Document());
            assertThat(result).isFalse();
        }

        @Test
        void shouldNotSkipPaymentsWhenNotRequestingAdjournment() {
            HearingBooking booking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(15))
                .build();

            CaseData caseData = CaseData.builder()
                .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                    .additionalApplicationType(List.of(C2_ORDER))
                    .temporaryC2Document(C2AdditionalApplicationEventData.builder()
                        .c2AdditionalOrdersRequested(List.of(CHANGE_SURNAME_OR_REMOVE_JURISDICTION))
                        .build())
                    .build())
                .build();

            boolean result = underTest.shouldSkipPayments(caseData, booking,
                caseData.getUploadAdditionalApplicationsEventData().getTemporaryC2Document());
            assertThat(result).isFalse();
        }

        @Test
        void shouldNotSkipPaymentsWhenApplyingForAnOtherOrder() {
            HearingBooking booking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(15))
                .build();

            CaseData caseData = CaseData.builder()
                .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                    .additionalApplicationType(List.of(OTHER_ORDER))
                    .temporaryC2Document(C2AdditionalApplicationEventData.builder()
                        .c2AdditionalOrdersRequested(List.of(CHANGE_SURNAME_OR_REMOVE_JURISDICTION))
                        .build())
                    .build())
                .build();

            boolean result = underTest.shouldSkipPayments(caseData, booking,
                caseData.getUploadAdditionalApplicationsEventData().getTemporaryC2Document());
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
        void shouldSealOtherDocumentIgnoringNullSupplements() {
            OtherApplicationsBundle bundle = OtherApplicationsBundle.builder()
                .document(DOCUMENT)
                .supplementsBundle(null)
                .build();
            OtherApplicationsBundle converted = underTest.convertOtherBundle(bundle, CASE_DATA);

            assertThat(converted.getDocument())
                .isEqualTo(SEALED_DOCUMENT);
            assertThat(converted.getSupplementsBundle()).isEmpty();
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
                .uploadAdditionalApplicationsEventData(UploadAdditionalApplicationsEventData.builder()
                    .isC2Confidential(YesNo.YES)
                    .build())
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

    @Test
    public void shouldNotReturnErrorIfCTSCUserAndNoC2DraftOrdersUploaded() {
        given(user.isCtscUser()).willReturn(true);
        UploadAdditionalApplicationsEventData eventData = UploadAdditionalApplicationsEventData.builder()
            .temporaryC2Document(C2AdditionalApplicationEventData.builder().build())
            .build();

        List<String> errors = underTest.validateC2Bundle(eventData);

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReturnErrorIfNotCTSCUserAndNoC2DraftOrdersUploaded() {
        given(user.isCtscUser()).willReturn(false);

        UploadAdditionalApplicationsEventData eventData = UploadAdditionalApplicationsEventData.builder()
            .temporaryC2Document(C2AdditionalApplicationEventData.builder().build())
            .build();

        List<String> errors = underTest.validateC2Bundle(eventData);

        assertThat(errors).contains("Please upload a draft order to proceed");
    }

    @Test
    public void shouldReturnErrorIfNotCTSCUserAndMultipleC2DraftOrdersUploaded() {
        given(user.isCtscUser()).willReturn(false);

        UploadAdditionalApplicationsEventData eventData = UploadAdditionalApplicationsEventData.builder()
            .temporaryC2Document(C2AdditionalApplicationEventData.builder()
                .draftOrdersBundle(List.of(element(DraftOrder.builder().build()),
                    element(DraftOrder.builder().build())))
                .build())
            .build();

        List<String> errors = underTest.validateC2Bundle(eventData);

        assertThat(errors).contains("Please upload only a single draft order to proceed");
    }

    @Test
    public void shouldReturnListOfChildrenAsDynamicMultiSelectList() {
        UUID child1Id = UUID.randomUUID();
        UUID child2Id = UUID.randomUUID();

        CaseData caseData =  CaseData.builder().children1(createChildrenList(child1Id, child2Id)).build();

        DynamicMultiselectListElement child1Element = DynamicMultiselectListElement.builder()
            .label("Jemima Test (Child 1)")
            .code(child1Id.toString())
            .build();

        DynamicMultiselectListElement child2Element = DynamicMultiselectListElement.builder()
            .label("Jim Test (Child 2)")
            .code(child2Id.toString())
            .build();

        DynamicMultiSelectList expectedList = DynamicMultiSelectList.builder()
            .listItems(List.of(child1Element, child2Element))
            .build();

        assertThat(underTest.getChildrenMultiSelectList(caseData)).isEqualTo(expectedList);
    }

    private void assertOnlineC2DocumentBundle(C2DocumentBundle actualC2Bundle, Supplement expectedSupplement,
                                        SupportingEvidenceBundle expectedSupportingEvidence) {

        assertThat(actualC2Bundle.getId()).isNotNull();
        assertThat(actualC2Bundle.getDocument().getFilename()).isEqualTo(C2_APPLICATION_NAME);
        assertThat(actualC2Bundle.getType()).isEqualTo(WITH_NOTICE);
        assertThat(actualC2Bundle.getSupportingEvidenceBundle()).hasSize(1);
        assertThat(actualC2Bundle.getSupplementsBundle()).hasSize(1);
        assertThat(actualC2Bundle.getChildrenOnApplication()).isEqualTo("Jemima Test (Child 1)\nJim Test (Child 2)");

        assertSupplementsBundle(actualC2Bundle.getSupplementsBundle().get(0).getValue(), expectedSupplement);
        assertSupportingEvidenceBundle(
            actualC2Bundle.getSupportingEvidenceBundle().get(0).getValue(), expectedSupportingEvidence
        );
    }

    private void assertPaperC2DocumentBundle(C2DocumentBundle actualC2Bundle, Supplement expectedSupplement,
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
        return PBAPayment.builder()
            .usePbaPayment("Yes")
            .pbaNumberDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code("PBA12345")
                    .build())
                .build())
            .build();
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

    private C2AdditionalApplicationEventData createC2EventData(Supplement supplementsBundle,
                                                               SupportingEvidenceBundle supportingEvidenceBundle) {
        return C2AdditionalApplicationEventData.builder()
            .type(WITH_NOTICE)
            .document(DOCUMENT)
            .c2AdditionalOrdersRequested(List.of(C2AdditionalOrdersRequested.PARENTAL_RESPONSIBILITY))
            .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_SECOND_FEMALE_PARENT)
            .supportingEvidenceBundle(wrapElements(supportingEvidenceBundle))
            .supplementsBundle(wrapElements(supplementsBundle))
            .build();
    }

    private C2AdditionalApplicationEventData createC2EventDataForOnlineForm(Supplement supplementsBundle,
                                                               SupportingEvidenceBundle supportingEvidenceBundle) {
        DynamicMultiselectListElement child1Element = DynamicMultiselectListElement.builder()
            .label("Jemima Test (Child 1)")
            .code("67891")
            .build();

        DynamicMultiselectListElement child2Element = DynamicMultiselectListElement.builder()
            .label("Jim Test (Child 2)")
            .code("12345")
            .build();

        DynamicMultiSelectList childSelector = DynamicMultiSelectList.builder()
            .listItems(List.of(child1Element, child2Element))
            .value(List.of(child1Element, child2Element))
            .build();

        return C2AdditionalApplicationEventData.builder()
            .type(WITH_NOTICE)
            .document(DOCUMENT)
            .c2AdditionalOrdersRequested(List.of(C2AdditionalOrdersRequested.PARENTAL_RESPONSIBILITY))
            .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_SECOND_FEMALE_PARENT)
            .supportingEvidenceBundle(wrapElements(supportingEvidenceBundle))
            .supplementsBundle(wrapElements(supplementsBundle))
            .childSelectorForApplication(childSelector)
            .applicationPermissionType(ApplicationPermissionType.NOT_REQUIRED)
            .applicationRelatesToAllChildren(YES)
            .applicationSummary("Blah Blah")
            .hasSafeguardingRisk(YES)
            .safeguardingRiskDetails("Details here")
            .isHearingAdjournmentRequired(NO)
            .canBeConsideredAtNextHearing(NO)
            .build();
    }

    private SupportingEvidenceBundle.SupportingEvidenceBundleBuilder createSupportingEvidenceBundleBuilder() {
        return createSupportingEvidenceBundleBuilder("Supporting document");
    }

    private SupportingEvidenceBundle.SupportingEvidenceBundleBuilder
        createSupportingEvidenceBundleBuilder(String name) {
        return SupportingEvidenceBundle.builder()
            .name(name)
            .notes("Document notes")
            .document(SUPPORTING_DOCUMENT)
            .dateTimeReceived(time.now().minusDays(1));
    }

    private SupportingEvidenceBundle createSupportingEvidenceBundle() {
        return createSupportingEvidenceBundle("Supporting document");
    }

    private SupportingEvidenceBundle createSupportingEvidenceBundle(String name) {
        return createSupportingEvidenceBundleBuilder(name).build();
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

    private List<Element<Child>> createChildrenList(UUID child1Id, UUID child2Id) {

        Child child1 = Child.builder()
            .party(ChildParty.builder()
                .firstName("Jemima")
                .lastName("Test")
                .build())
            .build();

        Child child2 = Child.builder()
            .party(ChildParty.builder()
                .firstName("Jim")
                .lastName("Test")
                .build())
            .build();

        return List.of(element(child1Id, child1), element(child2Id, child2));
    }
}
