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
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.service.notify.NotificationClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.NextHearingType.ISSUES_RESOLUTION_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createDraftCaseManagementOrder;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRecitals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createSchedule;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class ActionCMOControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final byte[] pdf = {1, 2, 3, 4, 5};
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private NotificationClient notificationClient;

    private Document document;

    @BeforeEach
    void setup() throws IOException {
        document = document();
        DocmosisDocument docmosisDocument = new DocmosisDocument("case-management-order.pdf", pdf);

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(any(), any(), any(), any())).willReturn(document);
    }

    @Test
    void aboutToStartShouldReturnCaseManagementOrder() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("caseManagementOrder", createDraftCaseManagementOrder());

        CallbackRequest request = buildCallbackRequest(data);

        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, "about-to-start");
        CaseData caseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getCaseManagementOrder()).isEqualTo(createDraftCaseManagementOrder());
    }

    @Test
    void midEventShouldReturnDocumentReferenceForAction() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("caseManagementOrder", createDraftCaseManagementOrder());
        data.put("hearingDetails", createHearingBookings(LocalDateTime.now()));

        CallbackRequest request = buildCallbackRequest(data);

        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, "mid-event");

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, pdf, "draft-case-management-order.pdf");

        assertThat(response.getData().get("orderAction")).extracting("orderDoc")
            .isEqualTo(ImmutableMap.of(
                "document_binary_url", document.links.binary.href,
                "document_filename", document.originalDocumentName,
                "document_url", document.links.self.href));
    }

    @Test
    void aboutToSubmitShouldReturnCaseManagementOrderWithActionAndSchedule() throws Exception {
        CaseManagementOrder caseManagementOrder = getCaseManagementOrder(OrderAction.builder().build());

        Map<String, Object> data = ImmutableMap.of(
            "caseManagementOrder", caseManagementOrder,
            "orderAction", getOrderAction(),
            "hearingDetails", createHearingBookings(LocalDateTime.now()));

        AboutToStartOrSubmitCallbackResponse response =
            makeRequest(buildCallbackRequest(data), "about-to-submit");

        CaseData caseData = objectMapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, pdf, "case-management-order.pdf");
        assertThat(caseData.getCaseManagementOrder().getAction()).isEqualTo(getOrderAction());
        assertThat(caseData.getCaseManagementOrder().getSchedule()).isEqualTo(createSchedule(true));
    }

    @Test
    void submittedShouldSendNotificationsWhenIssuedOrderApproved() throws Exception {
        CaseManagementOrder approvedCaseManagementOrder = getCaseManagementOrder(getOrderAction());

        Map<String, Object> data = ImmutableMap.of(
            "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER,
            "respondents1", createRespondents(),
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            "caseManagementOrder", approvedCaseManagementOrder);

        CallbackRequest callbackRequest = buildSubmittedCallbackRequest(data);

        makeSubmittedRequest(callbackRequest);

        verify(notificationClient, times(1)).sendEmail(
            eq(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE), eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eq(getExpectedCMOIssuedCaseLinkNotificationParameters()), eq("12345"));
    }

    @Test
    void submittedShouldNotSendNotificationsWhenIssuedOrderNotApproved() throws Exception {
        CaseManagementOrder caseManagementOrder = getCaseManagementOrder(OrderAction.builder().build());

        Map<String, Object> data = ImmutableMap.of(
            "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER,
            "respondents1", createRespondents(),
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            "orderAction", OrderAction.builder()
                .type(ActionType.JUDGE_REQUESTED_CHANGE)
                .nextHearingType(ISSUES_RESOLUTION_HEARING)
                .build(),
            "caseManagementOrder", caseManagementOrder);

        CallbackRequest callbackRequest = buildSubmittedCallbackRequest(data);

        makeSubmittedRequest(callbackRequest);

        verify(notificationClient, times(0)).sendEmail(
            eq(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE), eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eq(getExpectedCMOIssuedCaseLinkNotificationParameters()), eq("12345"));
    }

    private CallbackRequest buildSubmittedCallbackRequest(Map<String, Object> data) {
        return buildCallbackRequest(data);
    }

    private void makeSubmittedRequest(CallbackRequest request)
        throws Exception {
        mockMvc
            .perform(post("/callback/action-cmo/submitted")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }

    private CallbackRequest buildCallbackRequest(Map<String, Object> data) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(data)
                .build())
            .build();
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

    private ImmutableMap<String, Object> getExpectedCMOIssuedCaseLinkNotificationParameters() {
        final String subjectLine = "Jones, SACCCCCCCC5676576567";
        return ImmutableMap.<String, Object>builder()
            .put("localAuthorityNameOrRepresentativeFullName", LOCAL_AUTHORITY_NAME)
            .put("subjectLineWithHearingDate", subjectLine)
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();
    }
}
