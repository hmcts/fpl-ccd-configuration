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
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.NextHearingType.ISSUES_RESOLUTION_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCaseManagementOrder;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
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
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

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

        CallbackRequest request = buildCallbackRequest(data);

        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, "about-to-start");
        CaseData caseData = objectMapper.convertValue(response.getData(), CaseData.class);

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

        CaseManagementOrder order = getCaseManagementOrder(OrderAction.builder().build());

        Map<String, Object> data = ImmutableMap.of(
            CMO_TO_ACTION_KEY, order,
            "orderAction", getOrderAction());

        AboutToStartOrSubmitCallbackResponse response =
            makeRequest(buildCallbackRequest(data), "about-to-submit");

        CaseData caseData = objectMapper.convertValue(response.getData(), CaseData.class);

        verify(uploadDocumentService).uploadPDF(USER_ID, AUTH_TOKEN, pdf, "case-management-order.pdf");
        assertThat(caseData.getCmoToAction().getAction()).isEqualTo(getOrderAction());
    }

    private CallbackRequest buildCallbackRequest(Map<String, Object> data) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
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
}
