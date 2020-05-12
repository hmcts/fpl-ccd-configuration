package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.NextHearing;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderErrorMessages.HEARING_NOT_COMPLETED;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.NEXT_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.enums.NextHearingType.ISSUES_RESOLUTION_HEARING;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRecitals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createSchedule;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.ALLOCATED_JUDGE_KEY;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudge;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCaseManagementOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class ActionCaseManagementOrderControllerAboutToSubmitTest extends AbstractControllerTest {
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final UUID ID = randomUUID();

    private CaseDetails populatedCaseDetails;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Captor
    private ArgumentCaptor<DocmosisCaseManagementOrder> capturedData;

    ActionCaseManagementOrderControllerAboutToSubmitTest() {
        super("action-cmo");
    }

    @BeforeEach
    void setUp() {
        DocmosisDocument docmosisDocument = new DocmosisDocument("case-management-order.pdf", PDF);

        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(docmosisDocument);

        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document());

        populatedCaseDetails = populatedCaseDetails();
    }

    @Test
    void shouldReturnCaseManagementOrderWithFinalDocumentWhenSendToAllParties() {
        populatedCaseDetails.getData().putAll(
            Map.of(
                SCHEDULE.getKey(), createSchedule(true),
                RECITALS.getKey(), createRecitals(),
                HEARING_DETAILS_KEY, hearingBookingWithStartDatePlus(-1),
                CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), getCaseManagementOrder(),
                ORDER_ACTION.getKey(), getOrderAction(SEND_TO_ALL_PARTIES),
                NEXT_HEARING_DATE_LIST.getKey(), hearingDateList(),
                ALLOCATED_JUDGE_KEY, testJudge()));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(populatedCaseDetails);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(PDF, "case-management-order.pdf");
        verify(documentGeneratorService).generateDocmosisDocument(capturedData.capture(), eq(DocmosisTemplates.CMO));

        DocmosisHearingBooking hearingBooking = capturedData.getValue().getHearingBooking();

        assertThat(hearingBooking.getHearingTime()).isEqualTo(expectedHearingTime());
        assertThat(hearingBooking.getPreHearingAttendance()).isEqualTo(expectedPreHearing());
        assertThat(caseData.getCaseManagementOrder()).isEqualToComparingFieldByField(expectedCaseManagementOrder());
    }

    @Test
    void shouldErrorIfHearingDateInFutureWhenSendToAllParties() {
        Map<String, Object> data = Map.of(
            HEARING_DETAILS_KEY, hearingBookingWithStartDatePlus(1),
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), getCaseManagementOrder(),
            ORDER_ACTION.getKey(), getOrderAction(SEND_TO_ALL_PARTIES));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCaseDetails(data));

        assertThat(response.getErrors()).containsOnly(HEARING_NOT_COMPLETED.getValue());
    }

    @Test
    void shouldReturnCaseManagementOrderWithDraftDocumentWhenNotSendToAllParties() {
        populatedCaseDetails.getData().put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), getCaseManagementOrder());
        populatedCaseDetails.getData().put(ORDER_ACTION.getKey(), getOrderAction(JUDGE_REQUESTED_CHANGE));
        populatedCaseDetails.getData().put(HEARING_DETAILS_KEY, hearingBookingWithStartDatePlus(1));
        populatedCaseDetails.getData().put(ALLOCATED_JUDGE_KEY, testJudge());

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(populatedCaseDetails);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(PDF, "draft-case-management-order.pdf");
        assertThat(caseData.getCaseManagementOrder().getAction()).isEqualTo(getOrderAction(JUDGE_REQUESTED_CHANGE));
    }

    @Test
    void shouldRemoveOrderWhenOrderActionIsNotJudgeReview() {
        populatedCaseDetails.getData()
            .put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), CaseManagementOrder.builder().build());

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(populatedCaseDetails);

        assertThat(response.getData()).doesNotContainKey(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey());
    }

    @Test
    void shouldAllowJudiciaryToCompleteActionEventWhenNoCaseManagementOrder() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(populatedCaseDetails);

        assertThat(response.getData()).doesNotContainKey(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey());
    }

    private CaseManagementOrder expectedCaseManagementOrder() {
        return CaseManagementOrder.builder()
            .orderDoc(buildFromDocument(document()))
            .id(ID)
            .directions(emptyList())
            .action(OrderAction.builder()
                .type(SEND_TO_ALL_PARTIES)
                .nextHearingType(ISSUES_RESOLUTION_HEARING)
                .build())
            .nextHearing(NextHearing.builder()
                .id(ID)
                .date(formatTodayToMediumStyle())
                .build())
            .status(SEND_TO_JUDGE)
            .schedule(createSchedule(true))
            .recitals(createRecitals())
            .build();
    }

    private OrderAction getOrderAction(ActionType type) {
        return OrderAction.builder()
            .type(type)
            .nextHearingType(ISSUES_RESOLUTION_HEARING)
            .build();
    }

    private CaseManagementOrder getCaseManagementOrder() {
        return CaseManagementOrder.builder()
            .id(ID)
            .status(SEND_TO_JUDGE)
            .schedule(createSchedule(true))
            .recitals(createRecitals())
            .directions(createCmoDirections())
            .orderDoc(DocumentReference.builder().build())
            .build();
    }

    private List<Element<HearingBooking>> hearingBookingWithStartDatePlus(int days) {
        return List.of(element(ID, HearingBooking.builder()
            .startDate(now().plusDays(days))
            .endDate(now().plusDays(days))
            .venue("venue")
            .build()));
    }

    private DynamicList hearingDateList() {
        DynamicListElement listElement = DynamicListElement.builder()
            .label(formatTodayToMediumStyle())
            .code(ID)
            .build();

        return DynamicList.builder()
            .listItems(List.of(listElement))
            .value(listElement)
            .build();
    }

    private String formatTodayToMediumStyle() {
        return formatLocalDateToString(dateNow(), FormatStyle.MEDIUM);
    }

    private CaseDetails buildCaseDetails(Map<String, Object> data) {
        return CaseDetails.builder()
            .id(12345L)
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .data(data)
            .build();
    }

    private String expectedPreHearing() {
        return getStringDate(now().minusHours(1));
    }

    private String expectedHearingTime() {
        return getStringDate(now()) + " - " + getStringDate(now());
    }

    private String getStringDate(LocalDateTime now) {
        return formatLocalDateTimeBaseUsingFormat(now, "h:mma");
    }
}
