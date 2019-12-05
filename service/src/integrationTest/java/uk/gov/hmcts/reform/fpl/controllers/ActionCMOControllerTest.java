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
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.NextHearingType.ISSUES_RESOLUTION_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.Type.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createDraftCaseManagementOrder;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRecitals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createSchedule;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCMOController.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
class ActionCMOControllerTest {
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
        given(uploadDocumentService.uploadPDF(any(), any(), any(), any())).willReturn(document);
    }

    @Test
    void aboutToStartShouldReturnDraftCaseManagementOrderForAction() throws Exception {
        CallbackRequest request = buildCallbackRequest(createDraftCaseManagementOrder());

        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, "about-to-start");
        CaseData caseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getCaseManagementOrder()).isEqualTo(createDraftCaseManagementOrder());
    }

    @Test
    void midEventShouldReturnReviewedDocumentReferenceForAction() throws Exception {
        CallbackRequest request = buildCallbackRequest(createDraftCaseManagementOrder());

        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, "mid-event");

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, pdf, "draft-case-management-order.pdf");

        assertThat(response.getData()).containsKey(CASE_MANAGEMENT_ORDER_ACTION_KEY);

        assertThat(response.getData().get(CASE_MANAGEMENT_ORDER_ACTION_KEY)).extracting("orderDoc")
            .isEqualTo(ImmutableMap.of(
                "document_binary_url", document.links.binary.href,
                "document_filename", document.originalDocumentName,
                "document_url", document.links.self.href));
    }

    @Test
    void aboutToSubmitShouldReturnCaseDataWithApprovedCaseManagementOrder() throws Exception {
        OrderAction expectedAction = OrderAction.builder()
            .type(SEND_TO_ALL_PARTIES)
            .nextHearingType(ISSUES_RESOLUTION_HEARING)
            .build();

        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
            .status(CMOStatus.SEND_TO_JUDGE)
            .schedule(createSchedule(true))
            .recitals(createRecitals())
            .directions(createCmoDirections())
            .action(expectedAction)
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            makeRequest(buildCallbackRequest(caseManagementOrder), "about-to-submit");

        CaseData caseData = objectMapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, pdf, "case-management-order.pdf");
        assertThat(caseData.getCaseManagementOrder().getAction()).isEqualTo(expectedAction);
        assertThat(caseData.getCaseManagementOrder().getSchedule()).isEqualTo(createSchedule(true));
    }

    private CallbackRequest buildCallbackRequest(CaseManagementOrder caseManagementOrder) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of(CASE_MANAGEMENT_ORDER_KEY, caseManagementOrder,
                    "hearingDetails", createHearingBookings(LocalDateTime.now())))
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
}
