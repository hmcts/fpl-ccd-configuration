package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.fpl.testingsupport.controllers.TestingSupportController;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@ActiveProfiles("integration-test")
@WebMvcTest(TestingSupportController.class)
@OverrideAutoConfiguration(enabled = true)
class TestingSupportControllerTest {

    private static final String URL_TEMPLATE = "/testing-support/case/populate/%s";
    private static final long CASE_ID = 1L;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Test
    void shouldThrowExceptionForInvalidState() {
        Exception thrownException = assertThrows(NestedServletException.class,
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

    @ParameterizedTest
    @MethodSource("stateToEventNameSource")
    void shouldTriggerCorrectEvent(String state, String eventName) throws Exception {
        Map<String, Object> caseData = Map.of("property", "value");
        Map<String, Object> requestBody = Map.of("state", state, "caseData", caseData);

        var result = makePostRequest(requestBody);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            eventName,
            caseData);
    }

    private MvcResult makePostRequest(Map<String, Object> body) throws Exception {
        return mockMvc
            .perform(post(String.format(URL_TEMPLATE, CASE_ID))
                .header("authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body))
            ).andReturn();
    }

    private static Stream<Arguments> stateToEventNameSource() {
        return Stream.of(
            Arguments.of("GATEKEEPING", "populateCase-Gatekeeping"),
            Arguments.of("SUBMITTED", "populateCase-Submitted"),
            Arguments.of("PREPARE_FOR_HEARING", "populateCase-PREPARE_FOR_HEARING")
        );
    }
}
