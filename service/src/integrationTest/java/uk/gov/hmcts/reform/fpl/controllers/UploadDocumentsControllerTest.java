package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.INCLUDED_IN_SWET;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
public class UploadDocumentsControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldNotReturnErrorsWhenUploadedDocumentsAreValid() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(callbackRequest().getCaseDetails())
            .build();

        MvcResult response = performResponseCallBack(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors().size()).isEqualTo(0);
    }

    @Test
    void shouldReturnErrorsWhenUploadedDocumentsAreNotValid() throws Exception {
        CallbackRequest request = createCallbackRequest();
        MvcResult response = performResponseCallBack(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors().size()).isEqualTo(4);
        assertThat(callbackResponse.getErrors())
            .containsOnlyOnce("Check document 1. Attach the document or change the status from 'Attached'.",
                "Check document 3. Attach the SWET or change the status from 'Included in SWET'.",
                "Check document 5. Attach the document or change the status from 'Attached'.",
                "You must give additional document 1 a name.");
    }

    private CallbackRequest createCallbackRequest() {
        return CallbackRequest.builder().caseDetails(
            CaseDetails.builder()
                .data(ImmutableMap.of(
                    "documents_socialWorkChronology_document", ImmutableMap.of(
                        "documentStatus", ATTACHED.getLabel()
                    ),
                    "documents_socialWorkAssessment_document", ImmutableMap.of(
                        "documentStatus", INCLUDED_IN_SWET.getLabel()
                    ),
                    "documents_socialWorkEvidenceTemplate_document", ImmutableMap.of(
                        "documentStatus", ATTACHED.getLabel()
                    ),
                    "documents_socialWorkOther", ImmutableList.of(
                        ImmutableMap.of(
                            "id", UUID.randomUUID(),
                            "value", ImmutableMap.of(
                                "documentTitle", "")))
                )).build()).build();
    }

    private MvcResult performResponseCallBack(CallbackRequest request) throws Exception {
        return mockMvc
            .perform(post("/callback/upload-documents/mid-event")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }
}
