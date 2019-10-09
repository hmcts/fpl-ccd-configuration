package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ActiveProfiles("integration-test")
@WebMvcTest(C2UploadController.class)
@OverrideAutoConfiguration(enabled = true)
public class C2UploadControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnSuccessfulResponseWithValidCaseData() throws Exception {
        Document document = document();

        MvcResult response = mockMvc
            .perform(post("/callback/upload-c2/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("fixtures/case.json")))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getUploadC2()).hasSize(1);

        C2DocumentBundle expectedC2Document = caseData.getUploadC2().get(0).getValue();

        assertThat(expectedC2Document.getC2upload().getUrl()).isEqualTo(document.links.self.href);
        assertThat(expectedC2Document.getC2upload().getFilename()).isEqualTo(document.originalDocumentName);
        assertThat(expectedC2Document.getC2upload().getBinaryUrl()).isEqualTo(document.links.binary.href);
        assertThat(expectedC2Document.getC2UploadDescription()).isEqualTo("test");
    }
}
