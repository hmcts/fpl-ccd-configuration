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
import uk.gov.hmcts.reform.fpl.model.C21Order;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C21;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(C21OrderController.class)
@OverrideAutoConfiguration(enabled = true)
class C21OrderControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DateFormatterService dateFormatterService;

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
    class DateTests {

        @BeforeEach
        void setup() {
            given(dateFormatterService.formatLocalDateTimeBaseUsingFormat(any(), any()))
                .willReturn("1st November 2019");
            given(dateFormatterService.formatLocalDateToString(any(), any()))
                .willReturn("1st November 2019");
        }

        @Test
        void midEventShouldGenerateC21OrderDocument() throws Exception {
            byte[] pdf = {1, 2, 3, 4, 5};
            Document document = document();
            DocmosisDocument docmosisDocument = new DocmosisDocument(C21.getDocumentTitle(), pdf);

            given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
            given(uploadDocumentService.uploadPDF(USER_ID, AUTH_TOKEN, pdf, C21.getDocumentTitle() + "1.pdf"))
                .willReturn(document);

            AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(callbackRequest(), "mid-event");

            CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

            assertThat(caseData.getC21Order().getDocument()).isEqualTo(DocumentReference.builder()
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .url(document.links.self.href)
                .build());
        }

        @Test
        void aboutToSubmitShouldUpdateCaseData() throws Exception {
            AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(callbackRequest(), "about-to-submit");

            CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

            C21Order expectedOrder = C21Order.builder()
                .orderTitle("Example Order")
                .orderDetails("Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
                .orderDate("1st November 2019")
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HER_HONOUR_JUDGE)
                    .judgeLastName("Judy")
                    .legalAdvisorName("Peter Parker")
                    .build())
                .build();

            List<Element<C21Order>> c21Orders = caseData.getC21Orders();

            assertThat(caseData.getC21Order()).isEqualTo(null);
            assertThat(caseData.getJudgeAndLegalAdvisor()).isEqualTo(null);
            assertThat(c21Orders.get(0).getValue()).isEqualTo(expectedOrder);
        }
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
}
