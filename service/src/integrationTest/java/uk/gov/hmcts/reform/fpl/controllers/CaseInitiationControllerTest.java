package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest
public class CaseInitiationControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnSuccessfulResponseWithValidCaseData() throws Exception {

    }
}
