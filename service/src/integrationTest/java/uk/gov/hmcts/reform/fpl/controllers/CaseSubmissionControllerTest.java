package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;


@RunWith(SpringRunner.class)
@WebMvcTest
public class CaseSubmissionControllerTest {

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private UploadDocumentService uploadDocumentService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnSuccessfulResponseWithValidCaseData() throws
        Exception {

        mockMvc
            .perform(post("/callback/case-submission")
                .header("authorization", "Bearer token")
                .header("user-id", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("fixtures/case.json")))
            .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnUnsuccessfulResponseWithNoData() throws Exception {
        mockMvc
            .perform(post("/callback/case-submission"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldReturnUnsuccessfulResponseWithMalformedData() throws
        Exception {
        mockMvc
            .perform(post("/callback/case-submission")
                .header("authorization", "Bearer token")
                .header("user-id", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("Mock"))
            .andExpect(status().is4xxClientError());
    }
}
