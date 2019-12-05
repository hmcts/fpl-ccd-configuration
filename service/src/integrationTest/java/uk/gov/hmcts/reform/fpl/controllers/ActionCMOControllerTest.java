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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.CMOActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMONextHearingType.ISSUES_RESOLUTION_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createDraftCaseManagementOrder;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createSchedule;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCMOController.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
public class ActionCMOControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String CASE_MANAGEMENT_ORDER_ACTION_KEY = "caseManagementOrderAction";
    private static final String CASE_MANAGEMENT_ORDER_KEY = "caseManagementOrder";

    private static final byte[] pdf = {1, 2, 3, 4, 5};

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    private Document document;

    @BeforeEach
    void setup() throws IOException {
        document = document();
        DocmosisDocument docmosisDocument = new DocmosisDocument("case-management-order.pdf", pdf);

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(any(),any(), any(), any())).willReturn(document);
    }

    @Test
    void aboutToStartShouldReturnDraftCaseManagementOrderForAction() throws Exception {
        CallbackRequest request = buildCallbackRequest(createDraftCaseManagementOrder());

        MvcResult response = makeRequest(request, "about-to-start");
        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            response.getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData convertedCasData = objectMapper.convertValue(callbackResponse.getData(), CaseData.class);

        CaseManagementOrder caseManagementOrder = convertedCasData.getCaseManagementOrder();
        assertThat(caseManagementOrder).isEqualTo(createDraftCaseManagementOrder());
    }

    @Test
    void midEventShouldReturnReviewedDocumentReferenceForAction() throws Exception {
        CallbackRequest request = buildCallbackRequest(createDraftCaseManagementOrder());

        MvcResult response = makeRequest(request, "mid-event");
        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            response.getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, pdf, "draft-case-management-order.pdf");

        Map<String, Object> responseCaseData = callbackResponse.getData();

        assertThat(responseCaseData).containsKey(CASE_MANAGEMENT_ORDER_ACTION_KEY);

        Map<String, Object> caseManagementOrderAction = (Map<String, Object>) responseCaseData.get(
            CASE_MANAGEMENT_ORDER_ACTION_KEY);
        assertThat(caseManagementOrderAction).containsEntry(
            "orderDoc", ImmutableMap.builder()
                .put("document_binary_url", document.links.binary.href)
                .put("document_filename", document.originalDocumentName)
                .put("document_url", document.links.self.href)
                .build());
    }

    @Test
    void aboutToSubmitShouldReturnCaseDataWithApprovedCaseManagementOrder() throws Exception {
        CaseManagementOrderAction expectedCaseManagementOrderAction = CaseManagementOrderAction.builder()
            .cmoActionType(SEND_TO_ALL_PARTIES)
            .cmoNextHearingType(ISSUES_RESOLUTION_HEARING)
            .build();
        CaseManagementOrder caseManagementOrder = createDraftCaseManagementOrder().toBuilder()
            .caseManagementOrderAction(expectedCaseManagementOrderAction)
            .build();

        CallbackRequest request = buildCallbackRequest(caseManagementOrder);

        MvcResult response = makeRequest(request, "about-to-submit");
        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            response.getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, pdf, "case-management-order.pdf");

        Map<String, Object> responseCaseData = callbackResponse.getData();
        CaseManagementOrder caseManagementOrderResponse = objectMapper.convertValue(
            responseCaseData.get(CASE_MANAGEMENT_ORDER_KEY), CaseManagementOrder.class);

        assertThat(caseManagementOrderResponse).isNotNull();
        assertThat(caseManagementOrderResponse.getCaseManagementOrderAction())
            .isEqualTo(expectedCaseManagementOrderAction);
        assertThat(caseManagementOrderResponse.getSchedule()).isEqualTo(createSchedule(true));
    }

    private CallbackRequest buildCallbackRequest(CaseManagementOrder expectedCaseManagementOrder) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of(CASE_MANAGEMENT_ORDER_KEY, expectedCaseManagementOrder,
                    "hearingDetails", createHearingBookings(LocalDateTime.now())))
                .build())
            .build();
    }

    private MvcResult makeRequest(final CallbackRequest request, final String endpoint) throws Exception {
        return mockMvc
            .perform(post("/callback/action-cmo/" + endpoint)
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }
}
