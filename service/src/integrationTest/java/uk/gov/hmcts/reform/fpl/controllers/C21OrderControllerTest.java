package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.fpl.model.common.C21OrderBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.CreateC21OrderService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C21;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ActiveProfiles("integration-test")
@WebMvcTest(C21OrderController.class)
@OverrideAutoConfiguration(enabled = true)
class C21OrderControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String C21_DOCUMENT_TITLE = C21.getDocumentTitle();

    @MockBean
    private CreateC21OrderService createC21OrderService;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

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

        MvcResult response = makeRequest(request, "about-to-start");

        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            response.getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter Familyman case number");
    }

    @Test
    void midEventShouldGenerateC21OrderDocument() throws Exception {
        byte[] pdf = {1, 2, 3, 4, 5};
        DocmosisDocument docmosisDocument = new DocmosisDocument(C21_DOCUMENT_TITLE, pdf);
        Document document = document();

        CallbackRequest caseDetails = mapper.readValue(readBytes("fixtures/C21CaseData.json"),
            CallbackRequest.class);

        CaseData caseData = mapper.convertValue(caseDetails.getCaseDetails().getData(), CaseData.class);

        Map<String, Object> templateData = createTemplatePlaceholders();

        given(createC21OrderService.getC21OrderTemplateData(caseData))
            .willReturn(templateData);
        given(docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, C21))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(USER_ID, AUTH_TOKEN, pdf, C21_DOCUMENT_TITLE + "_1.pdf"))
            .willReturn(document);

        MvcResult response = makeRequest(caseDetails, "mid-event");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        C21Order c21Order = responseCaseData.getTemporaryC21Order();

        assertThat(c21Order.getC21OrderDocument().getUrl()).isEqualTo(document.links.self.href);
        assertThat(c21Order.getC21OrderDocument().getFilename()).isEqualTo(document.originalDocumentName);
        assertThat(c21Order.getC21OrderDocument().getBinaryUrl()).isEqualTo(document.links.binary.href);
    }

    @Test
    void aboutToSubmitShouldPopulateC21BundleAndRemoveTemporariesFromCaseData() throws Exception {
        byte[] pdf = {1, 2, 3, 4, 5};
        DocmosisDocument docmosisDocument = new DocmosisDocument(C21_DOCUMENT_TITLE, pdf);
        Document document = document();

        CallbackRequest caseDetails = mapper.readValue(readBytes("fixtures/C21CaseData.json"),
            CallbackRequest.class);

        CaseData caseData = mapper.convertValue(caseDetails.getCaseDetails().getData(), CaseData.class);

        Map<String, Object> templateData = createTemplatePlaceholders();

        given(createC21OrderService.getC21OrderTemplateData(caseData))
            .willReturn(templateData);
        given(docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, C21))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(USER_ID, AUTH_TOKEN, pdf, C21_DOCUMENT_TITLE + "_1.pdf"))
            .willReturn(document);

        MvcResult response = makeRequest(caseDetails, "about-to-submit");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(responseCaseData.getTemporaryC21Order()).isEqualTo(null);
        assertThat(responseCaseData.getJudgeAndLegalAdvisor()).isEqualTo(null);

        C21Order c21Order = caseData.getTemporaryC21Order();
        List<Element<C21OrderBundle>> c21OrderBundle = responseCaseData.getC21OrderBundle();

        given(createC21OrderService.appendToC21OrderBundle(c21Order, c21OrderBundle, caseData.getJudgeAndLegalAdvisor()))
            .willReturn(createOrderBundle(c21Order, c21OrderBundle, caseData.getJudgeAndLegalAdvisor()));

        assertThat(c21OrderBundle).hasSize(1);

        C21OrderBundle c21BundleEntry = c21OrderBundle.get(0).getValue();

        assertThat(c21BundleEntry.getOrderTitle()).isEqualTo(c21Order.getOrderTitle());
        assertThat(c21BundleEntry.getOrderDate()).isEqualTo("1st November 2019");
        assertThat(c21BundleEntry.getC21OrderDocument()).isEqualTo(c21Order.getC21OrderDocument());
        assertThat(c21BundleEntry.getJudgeTitleAndName()).isEqualTo(formatJudgeTitleAndName(
            caseData.getJudgeAndLegalAdvisor()));
    }

    private MvcResult makeRequest(CallbackRequest request, String endpoint) throws Exception {
        return mockMvc
            .perform(post("/callback/create-order/" + endpoint)
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }

    private Map<String, Object> createTemplatePlaceholders() {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", "SW123123")
            .put("courtName", "Family Court sitting at Swansea")
            .put("orderTitle", "Order")
            .put("orderDetails", "Example order details here - Lorem ipsum dolor sit amet, consectetur adipiscing elit")
            .put("todaysDate", "1st November 2019")
            .put("judgeTitleAndName", "Her Honour Judge Judy")
            .put("legalAdvisorName", "Peter Parker")
            .put("children", getChildrenDetails())
            .build();
    }

    private List<Map<String, String>> getChildrenDetails() {
        return ImmutableList.of(
            ImmutableMap.of(
                "gender", "Boy",
                "lastName", "Jones",
                "firstName", "Timothy",
                "dateOfBirth", "2015-08-01"));
    }


    private List<Element<C21OrderBundle>> createOrderBundle(
        C21Order c21Order, List<Element<C21OrderBundle>> c21OrderBundle, JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        c21OrderBundle.add(Element.<C21OrderBundle>builder()
            .id(UUID.randomUUID())
            .value(C21OrderBundle.builder()
                .orderTitle(c21Order.getOrderTitle())
                .orderDate("1st November 2019")
                .c21OrderDocument(c21Order.getC21OrderDocument())
                .judgeTitleAndName(formatJudgeTitleAndName(judgeAndLegalAdvisor))
                .build())
            .build());
        return c21OrderBundle;
    }

    private String formatJudgeTitleAndName(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (judgeAndLegalAdvisor == null || judgeAndLegalAdvisor.getJudgeTitle() == null) {
            return "";
        }

        if (judgeAndLegalAdvisor.getJudgeTitle() == MAGISTRATES) {
            return judgeAndLegalAdvisor.getJudgeFullName() + " (JP)";
        } else {
            return judgeAndLegalAdvisor.getJudgeTitle().getLabel() + " " + judgeAndLegalAdvisor.getJudgeLastName();
        }
    }
}
