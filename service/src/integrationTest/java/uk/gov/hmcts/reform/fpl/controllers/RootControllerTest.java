package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class RootControllerTest {

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private UploadDocumentService uploadDocumentService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @Autowired
    private transient MockMvc mockMvc;

    @Test
    public void should_welcome_upon_root_request_with_200_response_code() throws Exception {
        MvcResult response = mockMvc
            .perform(get("/"))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).startsWith("Welcome");
    }
}
