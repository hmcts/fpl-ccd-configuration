package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.calendar.client.BankHolidaysApi;
import uk.gov.hmcts.reform.calendar.model.BankHolidays;
import uk.gov.hmcts.reform.calendar.model.BankHolidays.Division;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;

@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class StandardDirectionsOrderControllerAboutToSubmitTest extends AbstractCallbackTest {
    private static final byte[] PDF = testDocumentBinaries();
    private static final String SEALED_ORDER_FILE_NAME = "standard-directions-order.pdf";
    private static final Document DOCUMENT = document();
    private static final DocumentReference DOCUMENT_REFERENCE = DocumentReference.buildFromDocument(DOCUMENT);
    private static final LocalDateTime HEARING_START_DATE = LocalDateTime.of(2020, 1, 20, 11, 11, 11);
    private static final LocalDateTime HEARING_END_DATE = LocalDateTime.of(2020, 2, 20, 11, 11, 11);
    private static final String DIRECTION_TYPE = "Identify alternative carers";
    private static final String DIRECTION_TEXT = "Contact the parents to make sure there is a complete family tree "
        + "showing family members who could be alternative carers.";
    private static final String LA_NAME = "example";
    private static final String COURT_NAME = "Family Court";
    private static final String COURT_CODE = "11";

    @MockBean
    private DocmosisDocumentGeneratorService docmosisService;

    @MockBean
    private BankHolidaysApi bankHolidaysApi;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocumentSealingService sealingService;

    @MockBean
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @Captor
    private ArgumentCaptor<String> filename;

    StandardDirectionsOrderControllerAboutToSubmitTest() {
        super("draft-standard-directions");
    }

    @BeforeEach
    void setup() {
        DocmosisDocument docmosisDocument = new DocmosisDocument(SEALED_ORDER_FILE_NAME, PDF);

        given(docmosisService.generateDocmosisDocument(any(DocmosisData.class), any())).willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(eq(PDF), filename.capture())).willReturn(DOCUMENT);

        given(docmosisService.generateDocmosisDocument(any(DocmosisNoticeOfProceeding.class), eq(C6)))
            .willReturn(docmosisDocument);

        given(uploadDocumentService.uploadPDF(PDF, C6.getDocumentTitle()))
            .willReturn(DOCUMENT);

        given(hmctsCourtLookupConfiguration.getCourt(LA_NAME))
            .willReturn(new HmctsCourtLookupConfiguration.Court(COURT_NAME, "hmcts-non-admin@test.com",
                COURT_CODE));
    }

    @Test
    void shouldPopulateHiddenCCDFieldsInStandardDirectionOrderToPersistData() {
        given(bankHolidaysApi.retrieveAll()) // there are no holidays :(
            .willReturn(BankHolidays.builder().englandAndWales(Division.builder().events(List.of()).build()).build());

        CaseData caseData = extractCaseData(postAboutToSubmitEvent(validSealedCaseDetailsForServiceRoute()));

        assertThat(caseData.getStandardDirectionOrder()).isEqualTo(expectedOrder());
        assertThat(caseData.getJudgeAndLegalAdvisor()).isNull();
        assertThatDirectionsArePlacedBackIntoCaseDetailsWithValues(caseData);
        assertThat(filename.getValue()).isEqualTo(SEALED_ORDER_FILE_NAME);
    }

    @Test
    void shouldReturnErrorsWhenNoHearingDetailsExistsForSealedOrder() {
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder().orderStatus(SEALED).build())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
            .allocatedJudge(Judge.builder().build())
            .localAuthorityDirections(buildDirections(Direction.builder().assignee(LOCAL_AUTHORITY).build()))
            .allParties(buildDirections(Direction.builder().assignee(ALL_PARTIES).build()))
            .respondentDirections(buildDirections(Direction.builder().assignee(PARENTS_AND_RESPONDENTS).build()))
            .cafcassDirections(buildDirections(Direction.builder().assignee(CAFCASS).build()))
            .otherPartiesDirections(buildDirections(Direction.builder().assignee(OTHERS).build()))
            .courtDirections(buildDirections(Direction.builder().assignee(COURT).build()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        assertThat(response.getErrors()).containsOnly("You need to enter a hearing date.");
    }

    @Test
    void shouldReturnErrorsWhenNoAllocatedJudgeExistsForSealedOrder() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(invalidCaseDetails());

        assertThat(response.getErrors()).containsOnly("You need to enter the allocated judge.");
    }

    @Test
    void shouldPopulateStandardDirectionOrderObjectFromUploadRoute() {
        DocumentReference order = DocumentReference.builder().filename("order.pdf").build();

        givenCurrentUserWithName("adam");

        CaseData data = extractCaseData(postAboutToSubmitEvent(validCaseDetailsForUploadRoute(order, DRAFT)));

        StandardDirectionOrder expected = StandardDirectionOrder.builder()
            .dateOfUpload(dateNow())
            .uploader("adam")
            .orderStatus(DRAFT)
            .orderDoc(order)
            .build();

        assertThat(data.getStandardDirectionOrder()).isEqualTo(expected);
    }

    @Test
    void shouldUpdateStateWhenOrderIsSealedThroughServiceRouteAndRemoveRouterAndSendNoticeOfProceedings() {
        given(bankHolidaysApi.retrieveAll()) // there are no holidays :(
            .willReturn(BankHolidays.builder().englandAndWales(Division.builder().events(List.of()).build()).build());

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(validSealedCaseDetailsForServiceRoute());

        CaseData responseCaseData = extractCaseData(response);

        DocumentReference noticeOfProceedingBundle = responseCaseData.getNoticeOfProceedingsBundle().get(0).getValue()
            .getDocument();

        assertThat(responseCaseData.getNoticeOfProceedingsBundle()).hasSize(1);
        assertThat(noticeOfProceedingBundle.getUrl()).isEqualTo(DOCUMENT.links.self.href);
        assertThat(noticeOfProceedingBundle.getFilename()).isEqualTo(DOCUMENT.originalDocumentName);
        assertThat(noticeOfProceedingBundle.getBinaryUrl()).isEqualTo(DOCUMENT.links.binary.href);

        assertThat(response.getData())
            .containsEntry("state", "PREPARE_FOR_HEARING")
            .doesNotContainKey("sdoRouter");
    }

    @Test
    void shouldUpdateStateAndOrderDocWhenSDOIsSealedThroughUploadRouteAndRemoveRouterAndSendNoticeOfProceedings() {
        DocumentReference sealedDocument = DocumentReference.builder().filename("sealed.pdf").build();
        DocumentReference document = DocumentReference.builder().filename("final.docx").build();

        givenCurrentUserWithName("adam");
        given(sealingService.sealDocument(document)).willReturn(sealedDocument);

        CaseData responseCaseData = extractCaseData(
            postAboutToSubmitEvent(validCaseDetailsForUploadRoute(document, SEALED))
        );

        assertThat(responseCaseData.getStandardDirectionOrder().getLastUploadedOrder()).isEqualTo(document);
        assertThat(responseCaseData.getNoticeOfProceedingsBundle()).hasSize(1).first()
            .extracting(element -> element.getValue().getDocument())
            .isEqualTo(DOCUMENT_REFERENCE);
        assertThat(responseCaseData.getState()).isEqualTo(State.CASE_MANAGEMENT);
        assertThat(responseCaseData.getSdoRouter()).isNull();
    }

    @Test
    void shouldRemoveTemporaryFields() {
        DocumentReference order = DocumentReference.builder().filename("order.pdf").build();

        givenCurrentUserWithName("adam");

        CaseDetails caseDetails = asCaseDetails(validCaseDetailsForUploadRoute(order, DRAFT));
        Map<String, Object> dataMap = new HashMap<>(caseDetails.getData());

        dataMap.putAll(Map.of(
            "dateOfIssue", dateNow(),
            "preparedSDO", DocumentReference.builder().build(),
            "currentSDO", DocumentReference.builder().build(),
            "replacementSDO", DocumentReference.builder().build(),
            "useServiceRoute", "",
            "useUploadRoute", "YES",
            "noticeOfProceedings", NoticeOfProceedings.builder().build()
        ));

        caseDetails.setData(dataMap);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).doesNotContainKeys(
            "judgeAndLegalAdvisor",
            "dateOfIssue",
            "preparedSDO",
            "currentSDO",
            "replacementSDO",
            "useServiceRoute",
            "useUploadRoute",
            "noticeOfProceedings"
        );
    }

    private void assertThatDirectionsArePlacedBackIntoCaseDetailsWithValues(CaseData caseData) {
        assertThat(unwrapElements(caseData.getAllParties())).containsOnly(fullyPopulatedDirection(ALL_PARTIES));

        List<Element<Direction>> localAuthorityDirections = caseData.getLocalAuthorityDirections();
        assertThat(unwrapElements(localAuthorityDirections)).containsOnly(fullyPopulatedDirection(LOCAL_AUTHORITY));

        List<Element<Direction>> respondentDirections = caseData.getRespondentDirections();
        assertThat(unwrapElements(respondentDirections)).containsOnly(fullyPopulatedDirection(PARENTS_AND_RESPONDENTS));

        assertThat(unwrapElements(caseData.getCafcassDirections())).containsOnly(fullyPopulatedDirection(CAFCASS));
        assertThat(unwrapElements(caseData.getOtherPartiesDirections())).containsOnly(fullyPopulatedDirection(OTHERS));
        assertThat(unwrapElements(caseData.getCourtDirections())).containsOnly(fullyPopulatedDirection(COURT));
    }

    private CaseData validSealedCaseDetailsForServiceRoute() {
        CaseData.CaseDataBuilder builder = CaseData.builder()
            .id(1234123412341234L)
            .state(GATEKEEPING)
            .dateOfIssue(dateNow())
            .noticeOfProceedings(buildNoticeOfProceedings())
            .standardDirectionOrder(StandardDirectionOrder.builder().orderStatus(SEALED).build())
            .hearingDetails(wrapElements(HearingBooking.builder()
                .startDate(now())
                .endDate(now().plusDays(1))
                .venue("EXAMPLE")
                .build()))
            .caseLocalAuthority(LA_NAME)
            .dateSubmitted(dateNow())
            .applicants(getApplicant())
            .familyManCaseNumber("1234")
            .orders(Orders.builder().orderType(List.of(CARE_ORDER)).build())
            .sdoRouter(SDORoute.SERVICE)
            .allParties(buildDirection(ALL_PARTIES))
            .localAuthorityDirections(buildDirection(LOCAL_AUTHORITY))
            .respondentDirections(buildDirection(PARENTS_AND_RESPONDENTS))
            .cafcassDirections(buildDirection(CAFCASS))
            .otherPartiesDirections(buildDirection(OTHERS))
            .courtDirections(buildDirection(COURT));

        buildJudgeAndLegalAdvisorDetails(builder);

        return builder.build();
    }

    private CaseData validCaseDetailsForUploadRoute(DocumentReference document, OrderStatus status) {
        CaseData.CaseDataBuilder builder = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder()
                .startDate(now())
                .endDate(now().plusDays(1))
                .venue("EXAMPLE")
                .build()))
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(status)
                .orderDoc(document)
                .build())
            .caseLocalAuthority(LA_NAME)
            .familyManCaseNumber("1234")
            .orders(Orders.builder().orderType(List.of(CARE_ORDER)).build())
            .noticeOfProceedings(buildNoticeOfProceedings())
            .sdoRouter(SDORoute.UPLOAD)
            .state(GATEKEEPING)
            .id(1234123412341234L);

        buildJudgeAndLegalAdvisorDetails(builder);

        return builder.build();
    }

    private CaseData invalidCaseDetails() {
        return CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder()
                .startDate(HEARING_START_DATE)
                .endDate(HEARING_END_DATE)
                .build()))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder()
                    .dateOfBirth(dateNow().plusDays(1))
                    .lastName("Moley")
                    .relationshipToChild("Uncle")
                    .build())
                .build()))
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(SEALED)
                .orderDoc(DocumentReference.builder().build())
                .build())
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .build();
    }

    private void buildJudgeAndLegalAdvisorDetails(CaseData.CaseDataBuilder builder) {
        builder.judgeAndLegalAdvisor(
            JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").legalAdvisorName("Chris Newport").build()
        ).allocatedJudge(
            Judge.builder().judgeTitle(MAGISTRATES).judgeFullName("John Walker").build()
        );
    }

    private List<Element<Direction>> buildDirection(DirectionAssignee assignee) {
        return wrapElements(Direction.builder()
            .directionType(DIRECTION_TYPE)
            .directionText(DIRECTION_TEXT)
            .assignee(assignee)
            .readOnly("Yes")
            .directionRemovable("Yes")
            .directionNeeded("Yes")
            .build());
    }

    private StandardDirectionOrder expectedOrder() {
        return StandardDirectionOrder.builder()
            .directions(fullyPopulatedDirections())
            .orderStatus(SEALED)
            .orderDoc(DocumentReference.builder()
                .url(DOCUMENT.links.self.href)
                .binaryUrl(DOCUMENT.links.binary.href)
                .filename("file.pdf")
                .build())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(MAGISTRATES)
                .judgeFullName("John Walker")
                .legalAdvisorName("Chris Newport")
                .build())
            .dateOfIssue(formatLocalDateToString(dateNow(), "d MMMM yyyy"))
            .build();
    }

    private Direction fullyPopulatedDirection(DirectionAssignee assignee) {
        return Direction.builder()
            .directionType(DIRECTION_TYPE)
            .directionText(DIRECTION_TEXT)
            .assignee(assignee)
            .directionRemovable("Yes")
            .directionNeeded("Yes")
            .readOnly("Yes")
            .dateToBeCompletedBy(dateNow().atStartOfDay())
            .build();
    }

    private List<Element<Direction>> fullyPopulatedDirections() {
        return Stream.of(DirectionAssignee.values())
            .map(assignee -> element(null, fullyPopulatedDirection(assignee)))
            .collect(toList());
    }

    private List<Element<Direction>> buildDirections(Direction direction) {
        return wrapElements(direction.toBuilder().directionType("Direction").build());
    }

    private List<Element<Applicant>> getApplicant() {
        return wrapElements(Applicant.builder().party(ApplicantParty.builder().organisationName("").build()).build());
    }

    private NoticeOfProceedings buildNoticeOfProceedings() {
        return NoticeOfProceedings.builder()
            .proceedingTypes(List.of(NOTICE_OF_PROCEEDINGS_FOR_PARTIES))
            .build();
    }
}
