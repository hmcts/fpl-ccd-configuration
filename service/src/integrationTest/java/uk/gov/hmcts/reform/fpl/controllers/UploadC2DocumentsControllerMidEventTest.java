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
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsControllerMidEventTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String USER_NAME = "Emma Taylor";
    private static final String ERROR_MESSAGE = "A document must be uploaded";
    private static final String DOCUMENT_URL = "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4";
    private static final String BINARY_URL = "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary";
    private static final String FILENAME = "file.pdf";

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void before() {
        given(userDetailsService.getUserName(AUTH_TOKEN))
            .willReturn(USER_NAME);
    }

    @Test
    void shouldReturnAnErrorWhenDocumentIsNotUploaded() throws Exception {
        CallbackRequest request = createCallbackRequestWithTempC2Bundle(null,null,null);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorWhenDocumentIsUploaded() throws Exception {
        CallbackRequest request = createCallbackRequestWithTempC2Bundle(DOCUMENT_URL,BINARY_URL,FILENAME);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    private CallbackRequest createCallbackRequestWithTempC2Bundle(String documentUrl,
                                                                  String binaryUrl,
                                                                  String filename) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of(
                    "temporaryC2Document",
                    documentOrEmptyMap(documentUrl, binaryUrl, filename)))
                .build())
            .build();
    }

    private ImmutableMap documentOrEmptyMap(String documentUrl, String binaryUrl, String filename) {
        if (isNull(documentUrl)) {
            return ImmutableMap.of();
        } else {
            return ImmutableMap.of(
                "document", ImmutableMap.of(
                    "document_url", documentUrl,
                    "document_binary_url", binaryUrl,
                    "document_filename", filename));
        }
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(CallbackRequest request) throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/upload-c2/mid-event")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}
