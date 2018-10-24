package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;


@RunWith(SpringRunner.class)
@WebMvcTest
public class CaseSubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnSuccessfulResponseWithValidCaseData() throws
        Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/case-submission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("fixtures/case.json")))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(response.getResponse().getContentAsString().equals(HttpStatus.OK));
    }

    @Test
    public void shouldReturnUnsuccessfulResponseWithNoData() throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/case-submission"))
            .andExpect(status().is4xxClientError())
            .andReturn();

        assertThat(response.getResponse().getContentAsString().equals(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void shouldReturnUnsuccessfulResponseWithMalformedData() throws
        Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/case-submission")
                .contentType(MediaType.APPLICATION_JSON)
                .content("Mock"))
            .andExpect(status().is4xxClientError())
            .andReturn();

        assertThat(response.getResponse().getContentAsString().equals(HttpStatus.BAD_REQUEST));
    }
}
