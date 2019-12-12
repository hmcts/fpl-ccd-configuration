package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.NextHearingType.ISSUES_RESOLUTION_HEARING;
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
    private static final String CMO_TO_ACTION_KEY = "cmoToAction";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final byte[] pdf = {1, 2, 3, 4, 5};
    private static final UUID NEXT_HEARING_ID = UUID.fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31");
    private static final LocalDateTime TODAYS_DATE = LocalDateTime.now();
    private final List<Element<HearingBooking>> hearingDetails = createHearingBookings(TODAYS_DATE);
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(
        FormatStyle.MEDIUM).localizedBy(Locale.UK);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @BeforeEach
    void setup() throws IOException {
        Document document = document();
        DocmosisDocument docmosisDocument = new DocmosisDocument("case-management-order.pdf", pdf);

        given(documentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(any(), any(), any(), any())).willReturn(document);
    }

    @Test
    void aboutToStartShouldExtractIndividualCaseManagementOrderFields() throws Exception {
        Map<String, Object> data = new HashMap<>();
        final CaseManagementOrder order = createCaseManagementOrder();

        data.put(CMO_TO_ACTION_KEY, order);
        data.put("hearingDetails", createHearingBookings(LocalDateTime.now()));

        CallbackRequest request = buildCallbackRequest(data);
        List<String> expected = Arrays.asList(
            TODAYS_DATE.plusDays(5).format(dateTimeFormatter),
            TODAYS_DATE.plusDays(2).format(dateTimeFormatter),
            TODAYS_DATE.format(dateTimeFormatter));

        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, "about-to-start");
        CaseData caseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(getHearingDates(response)).isEqualTo(expected);
        assertThat(caseData.getOrderAction()).isNull();
        assertThat(caseData.getSchedule()).isEqualTo(order.getSchedule());
        assertThat(caseData.getRecitals()).isEqualTo(order.getRecitals());
    }

    @Test
    void midEventShouldAddDocumentReferenceToOrderAction() throws Exception {
        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
            buildCallbackRequest(ImmutableMap.of()), "mid-event");

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, pdf, "draft-case-management-order.pdf");

        Map<String, Object> responseCaseData = callbackResponse.getData();

        OrderAction action = objectMapper.convertValue(responseCaseData.get("orderAction"), OrderAction.class);

        assertThat(action.getDocument()).isEqualTo(
            DocumentReference.builder()
                .binaryUrl(document().links.binary.href)
                .filename(document().originalDocumentName)
                .url(document().links.self.href)
                .build());
    }

    @Test
    void aboutToSubmitShouldReturnAPopulatedCaseManagementOrderWithUpdatedDocumentWhenSendToAllParties()
        throws Exception {
        DynamicList dynamicHearingDates = draftCMOService.buildDynamicListFromHearingDetails(hearingDetails);

        dynamicHearingDates.setValue(DynamicListElement.builder()
            .code(NEXT_HEARING_ID)
            .label(TODAYS_DATE.plusDays(5).toString())
            .build());

        CaseManagementOrder order = getCaseManagementOrder(OrderAction.builder().build());

        Map<String, Object> data = ImmutableMap.of(
            CMO_TO_ACTION_KEY, order,
            "hearingDetails", createHearingBookings(TODAYS_DATE),
            "orderAction", getOrderAction(),
            "nextHearingDateList", dynamicHearingDates);

        AboutToStartOrSubmitCallbackResponse response =
            makeRequest(buildCallbackRequest(data), "about-to-submit");

        CaseData caseData = objectMapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, pdf, "draft-case-management-order.pdf");

        assertThat(caseData.getCmoToAction().getAction()).isEqualTo(
            OrderAction.builder()
                .type(SEND_TO_ALL_PARTIES)
                .nextHearingId(NEXT_HEARING_ID)
                .nextHearingDate(TODAYS_DATE.plusDays(5).toString())
                .nextHearingType(ISSUES_RESOLUTION_HEARING)
                .build());

        String date = dateFormatterService.formatLocalDateTimeBaseUsingFormat(TODAYS_DATE, "d MMMM");
        String time = dateFormatterService.formatLocalDateTimeBaseUsingFormat(TODAYS_DATE, "h:mma");
        String expectedLabel = String.format("The next hearing date is on %s at %s", date, time);

        assertThat(response.getData().get("nextHearingDateLabel")).isEqualTo(expectedLabel);
    }

    @Test
    void submittedShouldTriggerCMOProgressionEvent() throws Exception {
        String event = "internal-change:CMO_PROGRESSION";
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(1L)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(ImmutableMap.of(
                    "cmoToAction", CaseManagementOrder.builder()
                        .status(SEND_TO_JUDGE)
                        .action(OrderAction.builder()
                            .type(SEND_TO_ALL_PARTIES)
                            .build())
                        .build()))
                .build())
            .build();
        makeRequest(request);
        verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, 1L, event);
    }

    private CallbackRequest buildCallbackRequest(Map<String, Object> data) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(data)
                .build())
            .build();
    }

    private void makeRequest(CallbackRequest request) throws Exception {
        mockMvc.perform(post("/callback/action-cmo/submitted")
            .header("authorization", AUTH_TOKEN)
            .header("user-id", USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
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
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readValue(
            response.getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }

    private OrderAction getOrderAction() {
        return OrderAction.builder()
            .type(SEND_TO_ALL_PARTIES)
            .nextHearingType(ISSUES_RESOLUTION_HEARING)
            .nextHearingId(NEXT_HEARING_ID)
            .build();
    }

    private CaseManagementOrder getCaseManagementOrder(OrderAction expectedAction) {
        return CaseManagementOrder.builder()
            .action(expectedAction)
            .status(CMOStatus.SEND_TO_JUDGE)
            .schedule(createSchedule(true))
            .recitals(createRecitals())
            .directions(createCmoDirections())
            .build();
    }

    private List<String> getHearingDates(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        CaseData caseData = objectMapper.convertValue(callbackResponse.getData(), CaseData.class);

        return caseData.getNextHearingDateList().getListItems().stream()
            .map(element -> objectMapper.convertValue(element, DynamicListElement.class))
            .map(DynamicListElement::getLabel).collect(Collectors.toList());
    }
}
