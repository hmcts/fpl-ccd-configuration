package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

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
            .hasHearings("Yes")
            .hasC2s("No")
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
            .hasHearings("No")
            .hasC2s("No")
            .build());
    }

    @Test
    void shouldInitialiseCourtBundleCollection() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();
        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));

        List<Element<CourtBundle>> courtBundleList = buildCourtBundleList(selectedHearingId);

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(today.plusDays(5), today.plusDays(6))),
            element(createHearingBooking(today.plusDays(2), today.plusDays(3))),
            element(createHearingBooking(today, today.plusDays(1))),
            element(selectedHearingId, selectedHearingBooking));

        DynamicList hearingList = ElementUtils
            .asDynamicList(hearingBookings, selectedHearingId, HearingBooking::toLabel);

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .courtBundleList(courtBundleList)
            .courtBundleHearingList(hearingList)
            .manageDocumentLA(buildManagementDocument(COURT_BUNDLE))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections", USER_ROLES);

        CaseData responseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        assertThat(responseData.getCourtBundleList()).isEqualTo(courtBundleList);

        assertThat(responseData.getManageDocumentLA()).isEqualTo(ManageDocumentLA.builder()
            .type(COURT_BUNDLE)
            .hasHearings("Yes")
            .hasC2s("No")
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
            .hasHearings("No")
            .hasC2s("Yes")
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
    }

    @Test
    void shouldThrowErrorWhenCourtBundleSelectedButNoHearingsFound() {
        CaseData caseData = CaseData.builder()
            .manageDocumentLA(buildManagementDocument(COURT_BUNDLE))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections", USER_ROLES);

        assertThat(callbackResponse.getErrors()).containsExactly("There are no hearings to associate a bundle with");
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
            .build());
    }

    private List<Element<CourtBundle>> buildCourtBundleList(UUID hearingId) {
        return List.of(element(hearingId, CourtBundle.builder().hearing("test hearing").build()));
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
}
