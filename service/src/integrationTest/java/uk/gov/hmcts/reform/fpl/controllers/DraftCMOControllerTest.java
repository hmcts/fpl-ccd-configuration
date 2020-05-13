package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRecital;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRepresentative;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRepresentedBy;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.UUID.fromString;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createElementCollection;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingsFromInitialDate;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createUnassignedDirection;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisJudge;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudge;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class DraftCMOControllerTest extends AbstractControllerTest {
    private static final long CASE_ID = 1L;
    private static final String DRAFT_CMO_FILE_NAME = "draft-case-management-order.pdf";

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private DocmosisDocumentGeneratorService generatorService;

    @MockBean
    private UploadDocumentService uploadService;

    @Captor
    private ArgumentCaptor<DocmosisCaseManagementOrder> captor;

    DraftCMOControllerTest() {
        super("draft-cmo");
    }

    @Test
    void aboutToStartCallbackShouldPrepareCaseForCMOWhenNoCaseManagementOrder() {
        Map<String, Object> data = Map.of(
            HEARING_DETAILS_KEY, createHearingBookingsFromInitialDate(now()),
            "respondents1", createRespondents(),
            "others", createOthers());

        List<String> expected = List.of(formatLocalDateToMediumStyle(5), formatLocalDateToMediumStyle(2));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(buildCaseDetails(data));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(getHearingDates(caseData.getCmoHearingDateList().getListItems())).isEqualTo(expected);

        assertThat(callbackResponse.getData().get("respondents_label"))
            .isEqualTo("Respondent 1 - Timothy Jones\nRespondent 2 - Sarah Simpson\n");

        assertThat(callbackResponse.getData().get("others_label"))
            .isEqualTo("Person 1 - Kyle Stafford\nOther person 1 - Sarah Simpson\n");

        assertThat(callbackResponse.getData()).doesNotContainKeys("schedule", "recitals", "orderAction");
    }

    @Test
    void aboutToStartCallbackShouldAddCCDFieldsWhenCaseManagementOrderIsNotNull() {
        Schedule schedule = Schedule.builder().includeSchedule("Yes").build();
        List<Element<Recital>> recitals = wrapElements(Recital.builder().title("title").build());
        OrderAction action = OrderAction.builder().type(ActionType.SELF_REVIEW).build();
        Direction direction = Direction.builder().assignee(ALL_PARTIES).directionType("title").build();
        List<Element<Direction>> directions = wrapElements(direction);

        CaseManagementOrder order = CaseManagementOrder.builder()
            .schedule(schedule)
            .recitals(recitals)
            .action(action)
            .directions(directions)
            .build();

        Map<String, Object> data = Map.of(
            HEARING_DETAILS_KEY, createHearingBookingsFromInitialDate(now()),
            "respondents1", createRespondents(),
            "others", createOthers(),
            CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), order);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(buildCaseDetails(data));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getSchedule()).isEqualTo(schedule);
        assertThat(caseData.getRecitals()).isEqualTo(recitals);
        assertThat(caseData.getOrderAction()).isEqualTo(action);
        assertThat(caseData.getAllParties()).isEqualTo(directions);
    }

    @Test
    void midEventShouldGenerateDraftCaseManagementOrderDocument() {
        byte[] pdf = {1, 2, 3, 4, 5};
        DocmosisDocument docmosisDocument = new DocmosisDocument("case-management-order.pdf", pdf);

        given(generatorService.generateDocmosisDocument(captor.capture(), any())).willReturn(docmosisDocument);
        given(uploadService.uploadPDF(any(), any())).willReturn(document());

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(getCaseDetails());

        verify(uploadService).uploadPDF(pdf, DRAFT_CMO_FILE_NAME);

        assertThat(callbackResponse.getData()).containsKey(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey());
        assertThat(getDocumentReference(callbackResponse)).isEqualTo(expectedDocument());
        assertThat(captor.getValue()).isEqualToComparingFieldByField(expectedTemplateData());
    }

    @Test
    void aboutToSubmitShouldPopulateCaseManagementOrder() {
        CaseDetails caseDetails = prepareCaseDetailsForAboutToSubmit();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        CaseManagementOrder caseManagementOrder = caseData.getCaseManagementOrder();

        assertThat(caseManagementOrder.getDirections()).containsAll(createCmoDirections());
        assertThat(caseManagementOrder.getId()).isEqualTo(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"));
        assertThat(caseManagementOrder.getHearingDate()).isEqualTo(formatLocalDateToMediumStyle(5));
        assertThat(caseManagementOrder.getStatus()).isEqualTo(SELF_REVIEW);
        assertThat(caseManagementOrder.getOrderDoc().getFilename()).isEqualTo(DRAFT_CMO_FILE_NAME);
        assertThat(caseManagementOrder.getAction().getChangeRequestedByJudge()).isEqualTo("Changes");
    }

    @Test
    void submittedShouldTriggerCMOProgressionEvent() {
        postSubmittedEvent(buildSubmittedRequest());

        verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, CASE_ID, "internal-change:CMO_PROGRESSION");
    }

    private List<String> getHearingDates(List<DynamicListElement> dynamicListElements) {
        return dynamicListElements.stream().map(DynamicListElement::getLabel).collect(Collectors.toList());
    }

    private CaseDetails buildCaseDetails(Map<String, Object> data) {
        return CaseDetails.builder()
            .id(CASE_ID)
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .data(data)
            .build();
    }

    private CaseDetails getCaseDetails() {
        CaseDetails caseDetails = populatedCaseDetails();

        caseDetails.getData().put("cmoHearingDateList", DynamicList.builder()
            .value(DynamicListElement.builder().code(UUID.fromString("51d02c7f-2a51-424b-b299-a90b98bb1774")).build())
            .build());

        caseDetails.getData().put("recitals", wrapElements(Recital.builder()
            .title("example recital")
            .description("description")
            .build()));

        caseDetails.getData().put("allPartiesCustomCMO", wrapElements(Direction.builder()
            .directionType("Example title")
            .directionText("Example text")
            .dateToBeCompletedBy(LocalDateTime.of(2099, 1, 1, 10, 0, 0))
            .build()));

        caseDetails.getData().put("allocatedJudge", testJudge());

        caseDetails.getData().put(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(),
            CaseManagementOrder.builder().build());

        caseDetails.getData().put("dateOfIssue", dateNow());

        return caseDetails;
    }

    private DocmosisCaseManagementOrder expectedTemplateData() {
        return DocmosisCaseManagementOrder.builder()
            .familyManCaseNumber("12345")
            .courtName("Family Court")
            .judgeAndLegalAdvisor(expectedJudgeAndLegalAdvisor())
            .allocatedJudge(testDocmosisJudge())
            .dateOfIssue(formatLocalDateToString(dateNow(), FormatStyle.LONG))
            .complianceDeadline("18 September 2020")
            .representatives(expectedRepresentatives())
            .respondents(expectedRespondents())
            .respondentsProvided(true)
            .children(expectedChildren())
            .applicantName("London Borough of Southwark")
            .hearingBooking(expectedHearing())
            .crest("[userImage:crest.png]")
            .draftbackground("[userImage:draft-watermark.png]")
            .recitals(expectedRecitals())
            .recitalsProvided(true)
            .directions(expectedDirections())
            .build();
    }

    private List<DocmosisDirection> expectedDirections() {
        return List.of(DocmosisDirection.builder()
            .assignee(ALL_PARTIES)
            .title("2. Example title by 10:00am, 1 January 2099")
            .body("Example text")
            .build());
    }

    private List<DocmosisRecital> expectedRecitals() {
        return List.of(DocmosisRecital.builder()
            .title("example recital")
            .body("description")
            .build());
    }

    private DocmosisHearingBooking expectedHearing() {
        String willAppearOnIssuedCMO = "This will appear on the issued CMO";

        return DocmosisHearingBooking.builder()
            .hearingDate(willAppearOnIssuedCMO)
            .hearingVenue(willAppearOnIssuedCMO)
            .preHearingAttendance(willAppearOnIssuedCMO)
            .hearingTime(willAppearOnIssuedCMO)
            .build();
    }

    private List<DocmosisChild> expectedChildren() {
        return List.of(
            DocmosisChild.builder()
                .name("Tom Reeves")
                .gender("Boy")
                .dateOfBirth("15 June 2018")
                .build(),
            DocmosisChild.builder()
                .name("Sarah Reeves")
                .gender("Girl")
                .dateOfBirth("2 February 2002")
                .build());
    }

    private List<DocmosisRespondent> expectedRespondents() {
        return List.of(
            DocmosisRespondent.builder()
                .name("Paul Smith")
                .relationshipToChild("Uncle")
                .build(),
            DocmosisRespondent.builder()
                .name("James Smith")
                .relationshipToChild("Brother")
                .build(),
            DocmosisRespondent.builder()
                .name("An Other")
                .relationshipToChild("Cousin")
                .build());
    }

    private DocmosisJudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor() {
        return DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName("His Honour Judge Walker")
            .legalAdvisorName("John Smith")
            .build();
    }

    private List<DocmosisRepresentative> expectedRepresentatives() {
        return List.of(DocmosisRepresentative.builder()
            .name("London Borough of Southwark")
            .representedBy(List.of(DocmosisRepresentedBy.builder()
                .name("Brian Banks")
                .email("brian@banks.com")
                .phoneNumber("020 2772 5772")
                .build()))
            .build());
    }

    private DocumentReference expectedDocument() {
        Document document = document();

        return DocumentReference.builder()
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .url(document.links.self.href)
            .build();
    }

    private DocumentReference getDocumentReference(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        Map<String, Object> responseCaseData = callbackResponse.getData();

        return mapper.convertValue(responseCaseData.get(
            CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey()), CaseManagementOrder.class).getOrderDoc();
    }

    private CaseDetails prepareCaseDetailsForAboutToSubmit() {
        Map<String, Object> data = new HashMap<>();

        Stream.of(DirectionAssignee.values()).forEach(direction ->
            data.put(direction.toCustomDirectionField().concat("CMO"),
                createElementCollection(createUnassignedDirection()))
        );

        data.put(HEARING_DATE_LIST.getKey(), getDynamicList());
        data.put(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), CaseManagementOrder.builder()
            .orderDoc(DocumentReference.builder().filename(DRAFT_CMO_FILE_NAME).build())
            .status(SELF_REVIEW)
            .action(OrderAction.builder().changeRequestedByJudge("Changes").build())
            .build());

        return buildCaseDetails(data);
    }

    private DynamicList getDynamicList() {
        DynamicListElement listElement = DynamicListElement.builder()
            .label(formatLocalDateToMediumStyle(5))
            .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
            .build();

        return DynamicList.builder()
            .listItems(List.of(
                DynamicListElement.builder()
                    .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                    .label(formatLocalDateToMediumStyle(5))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                    .label(formatLocalDateToMediumStyle(2))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                    .label(formatLocalDateToMediumStyle(0))
                    .build()))
            .value(listElement)
            .build();
    }

    private CallbackRequest buildSubmittedRequest() {
        CaseManagementOrder order = CaseManagementOrder.builder().status(SELF_REVIEW).build();

        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(Map.of(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), order))
                .build())
            .build();
    }

    private String formatLocalDateToMediumStyle(int i) {
        return formatLocalDateToString(dateNow().plusDays(i), FormatStyle.MEDIUM);
    }
}
