package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.C21Order;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.CreateC21OrderService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C21;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ActiveProfiles("integration-test")
@WebMvcTest(C21OrderController.class)
@OverrideAutoConfiguration(enabled = true)
class C21OrderControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @MockBean
    private CreateC21OrderService service;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void aboutToStartShouldReturnErrorsWhenFamilymanNumberIsNotProvided() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of("data", "some data"))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request, "about-to-start");

        assertThat(callbackResponse.getErrors()).containsExactly("Enter Familyman case number");
    }

    @Nested
    class DocumentTests {
        byte[] pdf = {1, 2, 3, 4, 5};
        Document document = document();
        private final String C21_DOCUMENT_TITLE = C21.getDocumentTitle();
        CaseData emptyCaseData = CaseData.builder().build();
        C21Order emptyC21 = C21Order.builder().build();

        DocumentTests() throws IOException {
            //NO - OP
        }

        @BeforeEach
        void setup() {
            DocmosisDocument docmosisDocument = new DocmosisDocument(C21_DOCUMENT_TITLE, pdf);
            given(docmosisService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
        }

        @Test
        void midEventShouldGenerateC21OrderDocument() throws Exception {
            given(service.getDocument(AUTH_TOKEN, USER_ID, emptyCaseData))
                .willReturn(document);
            given(uploadDocumentService.uploadPDF(USER_ID, AUTH_TOKEN, pdf, C21_DOCUMENT_TITLE + "1.pdf"))
                .willReturn(document);

            given(service.addDocumentToC21(emptyCaseData, document))
                .willReturn(C21Order.builder()
                    .document(DocumentReference.builder()
                        .url(document.links.self.href)
                        .binaryUrl(document.links.binary.href)
                        .filename(document.originalDocumentName)
                        .build())
                    .build());

            CallbackRequest request2 = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .data(ImmutableMap.of("c21Order", C21Order.builder().build()))
                    .build())
                .build();

            CallbackRequest request = mapper.readValue(readBytes("core-case-data-store-api/callback-request.json"),
                CallbackRequest.class);

            AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request, "mid-event");

            CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

            C21Order c21Order = responseCaseData.getC21Order();

            assertThat(c21Order.getDocument()).isEqualTo(document);
        }
    }

    @Test
    void aboutToSubmitShouldUpdateC21ListAndCaseData() throws Exception {

        CallbackRequest request2 = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.<String, Object>builder()
                    .put("c21Order", C21Order.builder()
                        .orderTitle("Title")
                        .orderDetails("Details")
                        .orderDate("1st November 2019")
                        .build())
                    .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder()
                        .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                        .judgeLastName("Judy")
                        .build())
                    .build())
                .build())
            .build();

        CallbackRequest request = mapper.readValue(readBytes("core-case-data-store-api/callback-request.json"),
            CallbackRequest.class);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request, "about-to-submit");

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        C21Order c21Order = C21Order.builder()
            .orderTitle("Title")
            .orderDetails("Details")
            .orderDate("1st November 2019")
            .judgeTitleAndName("Her Honour Judge Judy")
            .build();

        List<Element<C21Order>> c21Orders = responseCaseData.getC21Orders();

        C21Order c21OrderFromList = c21Orders.get(0).getValue();

        assertThat(responseCaseData.getC21Order()).isEqualTo(null);
        assertThat(responseCaseData.getJudgeAndLegalAdvisor()).isEqualTo(null);
        assertThat(c21Orders).hasSize(1);
        assertThat(c21OrderFromList).isEqualTo(c21Order);
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(CallbackRequest request, String endpoint)
        throws Exception {
        MvcResult mvc = mockMvc
            .perform(post("/callback/create-order/" + endpoint)
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return mapper.readValue(mvc.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
//
//    private Map<String, Object> createTemplatePlaceholders() {
//        return ImmutableMap.<String, Object>builder()
//            .put("familyManCaseNumber", "SW123123")
//            .put("courtName", "Family Court sitting at Swansea")
//            .put("orderTitle", "Order")
//            .put("orderDetails", "Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing
//            elit")
//            .put("todaysDate", "1st November 2019")
//            .put("judgeTitleAndName", "Her Honour Judge Judy")
//            .put("legalAdvisorName", "Peter Parker")
//            .put("children", createChildrenDetails())
//            .build();
//    }
//
//    private List<Map<String, String>> createChildrenDetails() {
//        return ImmutableList.of(
//            ImmutableMap.of(
//                "gender", "Boy",
//                "lastName", "Jones",
//                "firstName", "Timothy",
//                "dateOfBirth", "2015-08-01"));
//    }
//
//    private List<Element<C21Orders>> createOrderBundle(
//        C21Order c21Order, JudgeAndLegalAdvisor judgeAndLegalAdvisor, List<Element<C21Orders>> c21Orders) {
//        c21Orders.add(Element.<C21Orders>builder()
//            .id(UUID.randomUUID())
//            .value(C21Orders.builder()
//                .document(c21Order.getC21OrderDocument())
//                .orderTitle(c21Order.getOrderTitle())
//                .orderDate("1st November 2019")
//                .judgeTitleAndName(JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor))
//                .build())
//            .build());
//        return c21Orders;
//    }
}
