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
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
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
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderErrorMessages.HEARING_NOT_COMPLETED;
import static uk.gov.hmcts.reform.fpl.enums.NextHearingType.ISSUES_RESOLUTION_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCaseManagementOrder;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRecitals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createSchedule;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCaseManagementOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class ActionCaseManagementOrderControllerTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final String CMO_TO_ACTION_KEY = "cmoToAction";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(
        FormatStyle.MEDIUM).localizedBy(Locale.UK);
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    public static final String CASE_ID = "12345";
    public static final String REPRESENTATIVES = "representatives";
    private static final UUID ID = randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DateFormatterService dateFormatterService;

    @Autowired
    private DraftCMOService draftCMOService;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private NotificationClient notificationClient;

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
        final CaseManagementOrder order = createCaseManagementOrder();

        data.put(CMO_TO_ACTION_KEY, order);
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
    void midEventShouldAddDocumentReferenceToOrderAction() throws Exception {
        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
            buildCallbackRequest(ImmutableMap.of()), "mid-event");

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, PDF, "draft-case-management-order.pdf");

        Map<String, Object> responseCaseData = callbackResponse.getData();

        OrderAction action = mapper.convertValue(responseCaseData.get("orderAction"), OrderAction.class);

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
            CMO_TO_ACTION_KEY, getCaseManagementOrder(),
            "orderAction", getOrderAction(SEND_TO_ALL_PARTIES),
            "nextHearingDateList", hearingDateList());

        AboutToStartOrSubmitCallbackResponse response =
            makeRequest(buildCallbackRequest(data), "about-to-submit");

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, PDF, "case-management-order.pdf");
        assertThat(caseData.getCmoToAction()).isEqualTo(expectedCaseManagementOrder());
        assertThat(response.getData().get("nextHearingDateLabel")).isEqualTo(expectedLabel());
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
            .status(SEND_TO_JUDGE)
            .build();
    }

    private String expectedLabel() {
        return String.format("The next hearing date is on %s",
            dateFormatterService.formatLocalDateTimeBaseUsingFormat(NOW.minusDays(1), "d MMMM 'at' h:mma"));
    }

    @Test
    void aboutToSubmitShouldErrorIfHearingDateInFutureWhenSendToAllParties() throws Exception {
        Map<String, Object> data = ImmutableMap.of(
            "hearingDetails", hearingBookingWithStartDatePlus(1),
            CMO_TO_ACTION_KEY, getCaseManagementOrder(),
            "orderAction", getOrderAction(SEND_TO_ALL_PARTIES));

        AboutToStartOrSubmitCallbackResponse response =
            makeRequest(buildCallbackRequest(data), "about-to-submit");

        assertThat(response.getErrors()).containsOnly(HEARING_NOT_COMPLETED.getValue());
    }

    @Test
    void aboutToSubmitShouldReturnCaseManagementOrderWithDraftDocumentWhenNotSendToAllParties() throws Exception {
        Map<String, Object> data = ImmutableMap.of(
            CMO_TO_ACTION_KEY, getCaseManagementOrder(),
            "orderAction", getOrderAction(JUDGE_REQUESTED_CHANGE));

        AboutToStartOrSubmitCallbackResponse response =
            makeRequest(buildCallbackRequest(data), "about-to-submit");

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, PDF, "draft-case-management-order.pdf");
        assertThat(caseData.getCmoToAction().getAction()).isEqualTo(getOrderAction(JUDGE_REQUESTED_CHANGE));
    }

    @Test
    void submittedShouldTriggerCMOProgressionEventAndSendCaseLinkNotificationsWhenIssuedOrderApproved()
        throws Exception {
        String event = "internal-change:CMO_PROGRESSION";
        Map<String, Object> data = ImmutableMap.of(
            "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER,
            "respondents1", createRespondents(),
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            REPRESENTATIVES, createRepresentatives(DIGITAL_SERVICE),
            CMO_TO_ACTION_KEY, CaseManagementOrder.builder()
                .status(SEND_TO_JUDGE)
                .action(OrderAction.builder()
                    .type(SEND_TO_ALL_PARTIES)
                    .build())
                .build());

        CallbackRequest callbackRequest = buildCallbackRequest(data);

        makeRequest(callbackRequest);

        verify(coreCaseDataService)
            .triggerEvent(JURISDICTION, CASE_TYPE, 12345L, event);

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE), eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eq(getExpectedCMOIssuedCaseLinkNotificationParameters(LOCAL_AUTHORITY_NAME)), eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE), eq("abc@example.com"),
            eq(getExpectedCMOIssuedCaseLinkNotificationParameters("Jon Snow")), eq(CASE_ID));
    }

    @Test
    void submittedShouldNotSendNotificationsWhenIssuedOrderNotApproved() throws Exception {
        CaseManagementOrder caseManagementOrder = getCaseManagementOrder();

        Map<String, Object> data = ImmutableMap.of(
            "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER,
            "respondents1", createRespondents(),
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            CMO_TO_ACTION_KEY, caseManagementOrder.toBuilder()
                .action(getOrderAction(JUDGE_REQUESTED_CHANGE))
                .build());

        CallbackRequest callbackRequest = buildCallbackRequest(data);

        makeRequest(callbackRequest);

        verify(notificationClient, never()).sendEmail(
            eq(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE), eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eq(getExpectedCMOIssuedCaseLinkNotificationParameters(LOCAL_AUTHORITY_NAME)), eq(CASE_ID));
    }

    private CallbackRequest buildCallbackRequest(Map<String, Object> data) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(data)
                .build())
            .build();
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
            .status(CMOStatus.SEND_TO_JUDGE)
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

    private ImmutableMap<String, Object> getExpectedCMOIssuedCaseLinkNotificationParameters(String recipientName) {
        final String subjectLine = "Jones, SACCCCCCCC5676576567";
        return ImmutableMap.<String, Object>builder()
            .put("localAuthorityNameOrRepresentativeFullName", recipientName)
            .put("subjectLineWithHearingDate", subjectLine)
            .put("reference", CASE_ID)
            .put("caseUrl", String.format("http://fake-url/case/%s/%s/12345", JURISDICTION, CASE_TYPE))
            .build();
    }
}
