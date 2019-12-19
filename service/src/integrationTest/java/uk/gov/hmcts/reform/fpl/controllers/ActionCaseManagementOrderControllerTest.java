package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
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
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.JUDGE_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderErrorMessages.HEARING_NOT_COMPLETED;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.NEXT_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.NextHearingType.ISSUES_RESOLUTION_HEARING;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCaseManagementOrder;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRecitals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createSchedule;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCaseManagementOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class ActionCaseManagementOrderControllerTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(
        FormatStyle.MEDIUM).localizedBy(Locale.UK);
    private static final UUID ID = randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DraftCMOService draftCMOService;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @BeforeEach
    void setup() throws IOException {
        Document document = document();
        DocmosisDocument docmosisDocument = new DocmosisDocument("case-management-order.pdf", PDF);

        given(documentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(any(), any(), any(), any())).willReturn(document);
    }

    @Test
    void aboutToStartShouldExtractIndividualCaseManagementOrderFieldsWithFutureHearingDates() throws Exception {
        Map<String, Object> data = new HashMap<>();
        final CaseManagementOrder order = createCaseManagementOrder(JUDGE_REVIEW);

        data.put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order);
        data.put("hearingDetails", createHearingBookings(LocalDateTime.now()));

        CallbackRequest request = buildCallbackRequest(data);
        List<String> expected = Arrays.asList(
            NOW.plusDays(5).format(dateTimeFormatter),
            NOW.plusDays(2).format(dateTimeFormatter));

        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, "about-to-start");
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(getHearingDates(response)).isEqualTo(expected);
        assertThat(getHearingDates(response)).doesNotContain(NOW.format(dateTimeFormatter));
        assertThat(caseData.getOrderAction()).isNull();
        assertThat(caseData.getSchedule()).isEqualTo(order.getSchedule());
        assertThat(caseData.getRecitals()).isEqualTo(order.getRecitals());
    }

    @Test
    void aboutToStartShouldNotProgressOrderWhenOrderActionIsNotSet() throws Exception {
        CallbackRequest request = createRequestWithEmptyCMO();
        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, "about-to-start");
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getCaseManagementOrder()).isEqualTo(CaseManagementOrder.builder().build());
    }

    @Test
    void midEventShouldAddDocumentReferenceToOrderAction() throws Exception {
        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
            buildCallbackRequest(ImmutableMap.of()), "mid-event");

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, PDF, "draft-case-management-order.pdf");

        Map<String, Object> responseCaseData = callbackResponse.getData();

        OrderAction action = mapper.convertValue(responseCaseData.get(ORDER_ACTION.getKey()), OrderAction.class);

        assertThat(action.getDocument()).isEqualTo(
            DocumentReference.builder()
                .binaryUrl(document().links.binary.href)
                .filename(document().originalDocumentName)
                .url(document().links.self.href)
                .build());
    }

    @Test
    void aboutToSubmitShouldReturnCaseManagementOrderWithFinalDocumentWhenSendToAllParties() throws Exception {
        Map<String, Object> data = ImmutableMap.of(
            "hearingDetails", hearingBookingWithStartDatePlus(-1),
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), getCaseManagementOrder(),
            ORDER_ACTION.getKey(), getOrderAction(SEND_TO_ALL_PARTIES),
            NEXT_HEARING_DATE_LIST.getKey(), hearingDateList());

        AboutToStartOrSubmitCallbackResponse response =
            makeRequest(buildCallbackRequest(data), "about-to-submit");

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, PDF, "case-management-order.pdf");
        assertThat(caseData.getCaseManagementOrder()).isEqualTo(expectedCaseManagementOrder());
    }

    @Test
    void aboutToSubmitShouldErrorIfHearingDateInFutureWhenSendToAllParties() throws Exception {
        Map<String, Object> data = ImmutableMap.of(
            "hearingDetails", hearingBookingWithStartDatePlus(1),
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), getCaseManagementOrder(),
            ORDER_ACTION.getKey(), getOrderAction(SEND_TO_ALL_PARTIES));

        AboutToStartOrSubmitCallbackResponse response =
            makeRequest(buildCallbackRequest(data), "about-to-submit");

        assertThat(response.getErrors()).containsOnly(HEARING_NOT_COMPLETED.getValue());
    }

    @Test
    void aboutToSubmitShouldRemoveOrderWhenOrderActionIsNotJudgeReview() throws Exception {
        CallbackRequest request = createRequestWithEmptyCMO();
        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, "about-to-submit");
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getCaseManagementOrder()).isEqualTo(null);
    }

    @Test
    void submittedShouldTriggerCMOProgressionEvent() throws Exception {
        String event = "internal-change:CMO_PROGRESSION";
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(1L)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(ImmutableMap.of(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(),
                    CaseManagementOrder.builder().status(JUDGE_REVIEW).build()))
                .build())
            .build();

        makeRequest(request);

        verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, 1L, event);
    }

    private CaseManagementOrder expectedCaseManagementOrder() throws IOException {
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
                .date(NOW.toString())
                .build())
            .status(JUDGE_REVIEW)
            .build();
    }

    private CallbackRequest buildCallbackRequest(Map<String, Object> data) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(data)
                .build())
            .build();
    }

    private CallbackRequest createRequestWithEmptyCMO() {
        Map<String, Object> data = new HashMap<>();
        final CaseManagementOrder order = CaseManagementOrder.builder().build();

        data.put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order);
        return buildCallbackRequest(data);
    }

    private void makeRequest(CallbackRequest request) throws Exception {
        mockMvc.perform(post("/callback/action-cmo/submitted")
            .header("authorization", AUTH_TOKEN)
            .header("user-id", USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(CallbackRequest request, String endpoint)
        throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/action-cmo/" + endpoint)
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return mapper
            .readValue(response.getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
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
            .status(JUDGE_REVIEW)
            .schedule(createSchedule(true))
            .recitals(createRecitals())
            .directions(createCmoDirections())
            .build();
    }

    private List<Element<HearingBooking>> hearingBookingWithStartDatePlus(int days) {
        return ImmutableList.of(Element.<HearingBooking>builder()
            .id(ID)
            .value(HearingBooking.builder()
                .startDate(NOW.plusDays(days))
                .endDate(NOW.plusDays(days))
                .venue("venue")
                .build())
            .build());
    }

    private List<String> getHearingDates(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        return caseData.getNextHearingDateList().getListItems().stream()
            .map(element -> mapper.convertValue(element, DynamicListElement.class))
            .map(DynamicListElement::getLabel).collect(toList());
    }

    private DynamicList hearingDateList() {
        DynamicList dynamicHearingDates = draftCMOService
            .buildDynamicListFromHearingDetails(hearingBookingWithStartDatePlus(0));

        dynamicHearingDates.setValue(DynamicListElement.builder()
            .code(ID)
            .label(NOW.toString())
            .build());
        return dynamicHearingDates;
    }
}
