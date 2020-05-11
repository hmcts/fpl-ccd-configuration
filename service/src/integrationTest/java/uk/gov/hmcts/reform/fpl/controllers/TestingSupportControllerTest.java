package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.NestedServletException;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.testingsupport.controllers.TestingSupportController;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@ActiveProfiles("integration-test")
@WebMvcTest(TestingSupportController.class)
@OverrideAutoConfiguration(enabled = true)
class TestingSupportControllerTest extends AbstractControllerTest {

    private static final String URL_TEMPLATE = "/testingSupport/populateCase/%s";
    private static final long CASE_ID = 1L;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    TestingSupportControllerTest() {
        super("populate-case");
    }

    @Test
    void shouldThrowExceptionForInvalidState() {
        NestedServletException thrownException = assertThrows(NestedServletException.class,
            () -> makePostRequest(Map.of("state", "NOT_A_REAL_STATE")));

        assertThat(thrownException.getMessage()).contains("No enum constant");
    }

    @Test
    void shouldTriggerSubmittedEvent() throws Exception {
        Map<String, Object> caseData = Map.of("property", "value");
        Map<String, Object> requestBody = Map.of("state", "SUBMITTED", "caseData", caseData);

        var result = makePostRequest(requestBody);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "populateCase-Submitted",
            caseData);
    }

    @Test
    void shouldTriggerGatekeepingEvent() throws Exception {
        Map<String, Object> caseData = Map.of("property", "value");
        Map<String, Object> requestBody = Map.of("state", "GATEKEEPING", "caseData", caseData);

        var result = makePostRequest(requestBody);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "populateCase-Gatekeeping",
            caseData);
    }

    private MvcResult makePostRequest(Map<String, Object> body) throws Exception {
        return mockMvc
            .perform(post(String.format(URL_TEMPLATE, CASE_ID))
                .header("authorization", USER_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body))
            ).andReturn();
    }
}
