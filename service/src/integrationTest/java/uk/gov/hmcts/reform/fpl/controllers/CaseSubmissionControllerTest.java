package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.service.CaseRepository;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private UploadDocumentService uploadDocumentService;
    @MockBean
    private CaseRepository caseRepository;
    @MockBean
    private UserDetailsService userDetailsService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnSuccessfulResponseWithValidCaseData() throws Exception {
        byte[] pdf = {1, 2, 3, 4, 5};
        Document document = document();

        given(documentGeneratorService.generateSubmittedFormPDF(any()))
            .willReturn(pdf);
        given(uploadDocumentService.uploadPDF(USER_ID, AUTH_TOKEN, pdf, "2313.pdf"))
            .willReturn(document);

        mockMvc
            .perform(post("/callback/case-submission/submitted")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("fixtures/case.json")))
            .andExpect(status().isOk());

        Thread.sleep(3000);
        verify(caseRepository).setSubmittedFormPDF(AUTH_TOKEN, USER_ID, "2313", document);
    }

    @Test
    void shouldReturnUnsuccessfulResponseWithNoData() throws Exception {
        mockMvc
            .perform(post("/callback/case-submission/submitted")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturnUnsuccessfulResponseWithMalformedData() throws Exception {
        mockMvc
            .perform(post("/callback/case-submission/submitted")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("Mock"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldAddConsentLabelToCaseDetails() throws Exception {
        given(userDetailsService.getUserName(AUTH_TOKEN)).willReturn("Emma Taylor");

        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put("caseName", "title")
                .build()).build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/case-submission/about-to-start")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getData())
            .containsEntry("caseName", "title")
            .containsEntry("submissionConsentLabel",
                "I, Emma Taylor, believe that the facts stated in this application are true.");
    }
}
