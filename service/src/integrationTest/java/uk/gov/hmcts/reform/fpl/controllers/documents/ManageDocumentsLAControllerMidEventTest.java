package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.HearingDocumentType;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.representativeSolicitors;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.GUARDIAN_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeListLA.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeListLA.RESPONDENT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA.ADDITIONAL_APPLICATIONS_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA.CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA.HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA.PLACEMENT_NOTICE_RESPONSE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.DOCUMENT_ACKNOWLEDGEMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsLAControllerMidEventTest extends AbstractCallbackTest {

    private static final String USER_ROLES = "caseworker-publiclaw-solicitor";

    @MockBean
    private UserService userService;

    ManageDocumentsLAControllerMidEventTest() {
        super("manage-documents-la");
    }

    @Test
    void shouldInitialiseHearingList() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();
        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(today.plusDays(5), today.plusDays(6))),
            element(createHearingBooking(today.plusDays(2), today.plusDays(3))),
            element(createHearingBooking(today, today.plusDays(1))),
            element(selectedHearingId, selectedHearingBooking));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .hearingFurtherEvidenceDocuments(List.of(element(selectedHearingId, HearingFurtherEvidenceBundle.builder()
                .supportingEvidenceBundle(furtherEvidenceBundle)
                .build())))
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocumentLA(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections", USER_ROLES);

        CaseData responseData = extractCaseData(callbackResponse);

        DynamicList expectedDynamicList = ElementUtils
            .asDynamicList(hearingBookings, null, HearingBooking::toLabel);

        DynamicList hearingList = mapper.convertValue(responseData.getManageDocumentsHearingList(), DynamicList.class);

        assertThat(hearingList).isEqualTo(expectedDynamicList);

        assertThat(responseData.getManageDocumentLA()).isEqualTo(ManageDocumentLA.builder()
            .type(FURTHER_EVIDENCE_DOCUMENTS)
            .hasHearings(YES.getValue())
            .hasC2s(NO.getValue())
            .hasPlacementNotices(NO.getValue())
            .hasConfidentialAddress(NO.getValue())
            .build());
    }

    @Test
    void shouldInitialiseCorrespondenceCollection() {
        List<Element<SupportingEvidenceBundle>> correspondenceDocuments = buildSupportingEvidenceBundle();

        CaseData caseData = CaseData.builder()
            .correspondenceDocumentsLA(correspondenceDocuments)
            .manageDocumentLA(buildManagementDocument(CORRESPONDENCE)).build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections", USER_ROLES);

        CaseData responseData = extractCaseData(callbackResponse);
        assertThat(responseData.getCorrespondenceDocumentsLA()).isEqualTo(correspondenceDocuments);

        assertThat(responseData.getManageDocumentLA()).isEqualTo(ManageDocumentLA.builder()
            .type(CORRESPONDENCE)
            .hasHearings(NO.getValue())
            .hasC2s(NO.getValue())
            .hasPlacementNotices(NO.getValue())
            .hasConfidentialAddress(NO.getValue())
            .build());
    }

    @Test
    void shouldInitialiseCourtBundleCollection() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();
        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));

        List<Element<HearingCourtBundle>> courtBundleList = buildCourtBundleList(selectedHearingId);

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(today.plusDays(5), today.plusDays(6))),
            element(createHearingBooking(today.plusDays(2), today.plusDays(3))),
            element(createHearingBooking(today, today.plusDays(1))),
            element(selectedHearingId, selectedHearingBooking));

        DynamicList hearingList = ElementUtils
            .asDynamicList(hearingBookings, selectedHearingId, HearingBooking::toLabel);

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(courtBundleList).build())
            .hearingDocumentsHearingList(hearingList)
            .manageDocumentsHearingDocumentType(HearingDocumentType.COURT_BUNDLE)
            .manageDocumentLA(buildManagementDocument(HEARING_DOCUMENTS))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections", USER_ROLES);

        CaseData responseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        assertThat(responseData.getHearingDocuments().getCourtBundleListV2()).isEqualTo(courtBundleList);

        assertThat(responseData.getManageDocumentLA()).isEqualTo(ManageDocumentLA.builder()
            .type(HEARING_DOCUMENTS)
            .hasHearings(YES.getValue())
            .hasC2s(NO.getValue())
            .hasConfidentialAddress(NO.getValue())
            .hasPlacementNotices(NO.getValue())
            .build());
    }

    @Test
    void shouldInitialiseC2SupportingDocuments() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        LocalDateTime today = LocalDateTime.now();

        List<Element<SupportingEvidenceBundle>> c2EvidenceDocuments = buildSupportingEvidenceBundle();

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
            element(buildC2DocumentBundle(today.plusDays(2))),
            element(selectedC2DocumentId, buildC2DocumentBundle(c2EvidenceDocuments)),
            element(buildC2DocumentBundle(today.plusDays(2))));

        CaseData caseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .c2DocumentBundle(c2DocumentBundle)
            .manageDocumentLA(buildManagementDocument(ADDITIONAL_APPLICATIONS_DOCUMENTS))
            .manageDocumentsSupportingC2List(selectedC2DocumentId)
            .build();

        given(userService.hasAnyCaseRoleFrom(representativeSolicitors(), TEST_CASE_ID)).willReturn(false);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections", USER_ROLES);

        CaseData responseData = extractCaseData(callbackResponse);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(c2EvidenceDocuments);

        assertThat(responseData.getManageDocumentLA()).isEqualTo(ManageDocumentLA.builder()
            .type(ADDITIONAL_APPLICATIONS_DOCUMENTS)
            .hasHearings(NO.getValue())
            .hasC2s(YES.getValue())
            .hasPlacementNotices(NO.getValue())
            .hasConfidentialAddress(NO.getValue())
            .build());
    }

    @Test
    void shouldInitialisePlacementNoticeFields() {
        Placement placement = Placement.builder()
            .placementNotice(testDocumentReference())
            .noticeDocuments(emptyList())
            .childName("Test Child")
            .childId(randomUUID())
            .build();

        PlacementEventData eventData = PlacementEventData.builder()
            .placements(wrapElements(placement))
            .build();

        CaseData caseData = CaseData.builder()
            .manageDocumentLA(buildManagementDocument(PLACEMENT_NOTICE_RESPONSE))
            .placementEventData(eventData)
            .placementList(asDynamicList(eventData.getPlacements(), null, Placement::getChildName))
            .build();

        CaseData after = extractCaseData(postMidEvent(caseData, "initialise-manage-document-collections", USER_ROLES));

        assertThat(after.getManageDocumentLA()).isEqualTo(ManageDocumentLA.builder()
            .type(PLACEMENT_NOTICE_RESPONSE)
            .hasHearings(NO.getValue())
            .hasC2s(NO.getValue())
            .hasPlacementNotices(YES.getValue())
            .hasConfidentialAddress(NO.getValue())
            .build());
    }

    @Test
    void shouldInitialiseC2ApplicationSupportingDocumentsWhenTheSelectedC2IsInAdditionalApplicationsBundles() {
        UUID selectedApplicationId = UUID.randomUUID();
        LocalDateTime today = LocalDateTime.now();

        List<Element<SupportingEvidenceBundle>> c2EvidenceDocuments = buildSupportingEvidenceBundle();

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(element(buildC2DocumentBundle(today.plusDays(2))));

        C2DocumentBundle selectedC2Application = C2DocumentBundle.builder()
            .id(selectedApplicationId)
            .supportingEvidenceBundle(c2EvidenceDocuments)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(today.plusDays(1), DATE_TIME))
            .build();

        CaseData caseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .c2DocumentBundle(c2DocumentBundle)
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(selectedC2Application).build()))
            .manageDocumentLA(buildManagementDocument(ADDITIONAL_APPLICATIONS_DOCUMENTS))
            .manageDocumentsSupportingC2List(selectedApplicationId)
            .build();

        given(userService.hasAnyCaseRoleFrom(representativeSolicitors(), TEST_CASE_ID)).willReturn(false);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections", USER_ROLES);

        CaseData responseData = extractCaseData(callbackResponse);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(c2EvidenceDocuments);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp()).hasSizeGreaterThan(0);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp().get(0).getValue().getDocumentAcknowledge())
            .isEqualTo(List.of(DOCUMENT_ACKNOWLEDGEMENT_KEY));
    }

    @Test
    void shouldInitialiseOtherApplicationSupportingDocumentsWhenTheSelectedIdIsInAdditionalApplicationsBundles() {
        UUID selectedApplicationId = UUID.randomUUID();
        LocalDateTime today = LocalDateTime.now();

        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = buildSupportingEvidenceBundle();

        C2DocumentBundle c2Application = buildC2DocumentBundle(today.plusDays(1));

        OtherApplicationsBundle selectedApplication = OtherApplicationsBundle.builder()
            .id(selectedApplicationId)
            .applicationType(OtherApplicationType.C12_WARRANT_TO_ASSIST_PERSON)
            .supportingEvidenceBundle(supportingEvidenceBundle)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(today.plusDays(1), DATE_TIME)).build();

        CaseData caseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(c2Application).otherApplicationsBundle(selectedApplication).build()))
            .manageDocumentLA(buildManagementDocument(ADDITIONAL_APPLICATIONS_DOCUMENTS))
            .manageDocumentsSupportingC2List(selectedApplicationId)
            .build();

        given(userService.hasAnyCaseRoleFrom(representativeSolicitors(), TEST_CASE_ID)).willReturn(false);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections", USER_ROLES);

        CaseData responseData = extractCaseData(callbackResponse);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(supportingEvidenceBundle);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp()).hasSizeGreaterThan(0);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp().get(0).getValue().getDocumentAcknowledge())
            .isEqualTo(List.of(DOCUMENT_ACKNOWLEDGEMENT_KEY));
    }

    @Test
    void shouldInitialiseFurtherEvidenceCollection() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();
        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(today.plusDays(5), today.plusDays(6))),
            element(createHearingBooking(today.plusDays(2), today.plusDays(3))),
            element(createHearingBooking(today, today.plusDays(1))),
            element(selectedHearingId, selectedHearingBooking));

        CaseData caseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingList(selectedHearingId)
            .hearingFurtherEvidenceDocuments(List.of(element(selectedHearingId, HearingFurtherEvidenceBundle.builder()
                .supportingEvidenceBundle(furtherEvidenceBundle)
                .build())))
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocumentSubtypeListLA(OTHER)
            .manageDocumentLA(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS))
            .build();

        given(userService.hasAnyCaseRoleFrom(representativeSolicitors(), TEST_CASE_ID)).willReturn(false);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "further-evidence-documents", USER_ROLES);

        CaseData responseData = extractCaseData(callbackResponse);

        DynamicList expectedDynamicList = ElementUtils
            .asDynamicList(hearingBookings, selectedHearingId, HearingBooking::toLabel);

        DynamicList hearingList = mapper.convertValue(responseData.getManageDocumentsHearingList(), DynamicList.class);

        assertThat(hearingList).isEqualTo(expectedDynamicList);

        assertThat(callbackResponse.getData().get(MANAGE_DOCUMENTS_HEARING_LABEL_KEY))
            .isEqualTo(selectedHearingBooking.toLabel());

        assertThat(responseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(furtherEvidenceBundle);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp()).hasSizeGreaterThan(0);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp().get(0).getValue().getDocumentAcknowledge())
            .isEqualTo(List.of(DOCUMENT_ACKNOWLEDGEMENT_KEY));
    }

    @Test
    void shouldInitialiseRespondentStatementCollection() {
        UUID selectedRespondentId = randomUUID();
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = buildSupportingEvidenceBundle();

        List<Element<Respondent>> respondents = List.of(
            element(selectedRespondentId, Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("David")
                    .lastName("Stevenson")
                    .build())
                .build()));

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .respondentStatementList(selectedRespondentId)
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocumentSubtypeListLA(RESPONDENT_STATEMENT)
            .respondentStatements(List.of(
                element(RespondentStatement.builder()
                    .respondentId(selectedRespondentId)
                    .supportingEvidenceBundle(supportingEvidenceBundle)
                    .build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "further-evidence-documents", USER_ROLES);

        CaseData responseData = extractCaseData(callbackResponse);

        DynamicList expectedRespondentStatementList = ElementUtils
            .asDynamicList(respondents, selectedRespondentId,
                respondent -> respondent.getParty().getFullName());

        DynamicList respondentDynamicList
            = mapper.convertValue(responseData.getRespondentStatementList(), DynamicList.class);

        assertThat(respondentDynamicList).isEqualTo(expectedRespondentStatementList);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(supportingEvidenceBundle);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp()).hasSizeGreaterThan(0);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp().get(0).getValue().getDocumentAcknowledge())
            .isEqualTo(List.of(DOCUMENT_ACKNOWLEDGEMENT_KEY));
    }

    @Test
    void shouldThrowErrorWhenCourtBundleSelectedButNoHearingsFound() {
        CaseData caseData = CaseData.builder()
            .manageDocumentsHearingDocumentType(HearingDocumentType.COURT_BUNDLE)
            .manageDocumentLA(buildManagementDocument(HEARING_DOCUMENTS))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections", USER_ROLES);

        assertThat(callbackResponse.getErrors()).containsExactly(
            "There are no hearings to associate a hearing document with");
    }

    @Test
    void shouldThrowErrorWhenAdditionalApplicationsDocumentsIsSelectedButNoApplicationBundlesFound() {
        CaseData caseData = CaseData.builder()
            .manageDocumentLA(buildManagementDocument(ADDITIONAL_APPLICATIONS_DOCUMENTS))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections", USER_ROLES);

        assertThat(callbackResponse.getErrors())
            .containsExactly("There are no additional applications to associate supporting documents with");
    }

    private ManageDocumentLA buildManagementDocument(ManageDocumentTypeListLA type) {
        return ManageDocumentLA.builder().type(type).build();
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
        return wrapElements(SupportingEvidenceBundle.builder()
            .name("test")
            .uploadedBy("kurt.swansea@gov.uk")
            .type(GUARDIAN_REPORTS)
            .document(DocumentReference.builder().build())
            .build());
    }

    private List<Element<HearingCourtBundle>> buildCourtBundleList(UUID hearingId) {
        List<Element<CourtBundle>> courtBundle = List.of(element(CourtBundle.builder().build()));
        return List.of(element(
            hearingId,
            HearingCourtBundle.builder()
                .hearing("Test hearing")
                .courtBundle(courtBundle)
                .build()));
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder()
            .id(UUID.randomUUID())
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(dateTime, DATE_TIME))
            .build();
    }

    private C2DocumentBundle buildC2DocumentBundle(List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {
        return buildC2DocumentBundle(now()).toBuilder()
            .supportingEvidenceBundle(supportingEvidenceBundle)
            .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<a href",
        "<a href=\"https://www.google.com\">Google</a>",
        "</a>",
        "Google<a>",
        "Google</a>",
        "Google<a",
        "THIS IS LINE1\n<aTHIS IS LINE2",
        "THIS IS LINE1\n</aTHIS IS LINE2",
        "THIS IS LINE1<a>\nTHIS IS LINE2",
        "THIS IS LINE1<a\nTHIS IS LINE2",
        "THIS IS LINE1\r<aTHIS IS LINE2",
        "THIS IS LINE1\r</aTHIS IS LINE2",
        "THIS IS LINE1<a>\rTHIS IS LINE2",
        "THIS IS LINE1<a\r\nTHIS IS LINE2",
        "THIS IS LINE1\r\n<aTHIS IS LINE2",
        "THIS IS LINE1\r\n</aTHIS IS LINE2",
        "THIS IS LINE1<a>\r\nTHIS IS LINE2",
        "THIS IS LINE1<a\r\nTHIS IS LINE2"
    })
    void shouldBlockHtmlInjectionInSWET(String includeInSWET) {
        CaseData caseData = CaseData.builder()
            .manageDocumentLA(ManageDocumentLA.builder().type(FURTHER_EVIDENCE_DOCUMENTS).build())
            .applicationDocuments(List.of(element(ApplicationDocument.builder()
                .includedInSWET(includeInSWET)
                .build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "final-check", USER_ROLES);
        assertThat(callbackResponse.getErrors())
            .isEqualTo(List.of("The data entered is not valid for your input in SWET\n"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "THIS IS LINE1",
        "THIS IS LINE1\nTHIS IS LINE2",
        "THIS IS LINE1\rTHIS IS LINE2",
        "THIS IS LINE1\r\nTHIS IS LINE2",
        "<1",
        "<1ABC",
        "<1\nABC",
        "<1\rABC",
        "<1\r\nABC",
        "ABC <1",
        "ABC\n<1",
        "ABC\r<1",
        "ABC\r\n<1"
    })
    void shouldPassHtmlInjectionValidationInSWET(String includeInSWET) {
        CaseData caseData = CaseData.builder()
            .manageDocumentLA(ManageDocumentLA.builder().type(FURTHER_EVIDENCE_DOCUMENTS).build())
            .applicationDocuments(List.of(element(ApplicationDocument.builder()
                .includedInSWET(includeInSWET)
                .build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "final-check", USER_ROLES);
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManageDocumentTypeListLA.class, names = {
        "CORRESPONDENCE",
        "ADDITIONAL_APPLICATIONS_DOCUMENTS",
        "HEARING_DOCUMENTS",
        "PLACEMENT_NOTICE_RESPONSE"
    })
    void shouldPassFinalCheckMidEvent(ManageDocumentTypeListLA type) {
        CaseData caseData = CaseData.builder()
            .manageDocumentLA(ManageDocumentLA.builder().type(type).build())
            .applicationDocuments(List.of(element(ApplicationDocument.builder()
                .build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "final-check", USER_ROLES);
        assertThat(callbackResponse.getErrors()).isEmpty();
    }
}