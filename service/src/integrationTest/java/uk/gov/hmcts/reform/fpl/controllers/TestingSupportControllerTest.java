package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.testingsupport.controllers.TestingSupportController;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.microsoft.applicationinsights.core.dependencies.http.HttpStatus.SC_OK;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@ActiveProfiles("integration-test")
@WebMvcTest(TestingSupportController.class)
@OverrideAutoConfiguration(enabled = true)
class TestingSupportControllerTest {

    private static final long CASE_ID = 1L;
    private static final String POPULATE_CASE_PATH = "/testing-support/case/populate/1";
    private static final String CREATE_CASE_PATH = "/testing-support/case/create";
    private static final String USER_ID = randomAlphanumeric(10);
    private static final String USER_AUTH_TOKEN = randomAlphanumeric(10);
    private static final String SERVICE_AUTH_TOKEN = randomAlphanumeric(10);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private CoreCaseDataApiV2 coreCaseDataApiV2;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private RequestData requestData;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void init() {
        when(requestData.authorisation()).thenReturn(USER_AUTH_TOKEN);
        when(requestData.userId()).thenReturn(USER_ID);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldThrowExceptionForInvalidState() {
        Exception thrownException = assertThrows(NestedServletException.class,
            () -> makePostRequest(POPULATE_CASE_PATH, Map.of("state", "NOT_A_REAL_STATE")));

        assertThat(thrownException.getMessage()).contains("Unable to map NOT_A_REAL_STATE to a case state");
    }

    @ParameterizedTest
    @MethodSource("stateToEventNameSource")
    void shouldTriggerCorrectEvent(String state, String eventName) throws Exception {
        Map<String, Object> caseData = Map.of("property", "value");
        Map<String, Object> requestBody = Map.of("state", state, "caseData", caseData);

        var result = makePostRequest(POPULATE_CASE_PATH, requestBody);

        assertThat(result.getResponse().getStatus()).isEqualTo(SC_OK);
        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            eventName,
            caseData);
    }

    @Test
    void shouldCreateCase() throws Exception {

        String eventName = "openCase";

        Map<String, Object> initialData = Map.of("caseName", "Test");
        Map<String, Object> caseData = Map.of("id", UUID.randomUUID().toString());

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(eventName)
            .token(randomAlphanumeric(10))
            .caseDetails(CaseDetails.builder().build())
            .build();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .build())
            .data(initialData)
            .ignoreWarning(false)
            .build();

        when(coreCaseDataApi.startCase(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, eventName))
            .thenReturn(startEventResponse);

        when(coreCaseDataApiV2.saveCase(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_TYPE, caseDataContent))
            .thenReturn(caseData);

        MvcResult result = makePostRequest(CREATE_CASE_PATH, initialData);

        assertThat(result.getResponse().getStatus())
            .isEqualTo(SC_OK);
        assertThat(result.getResponse().getContentAsString())
            .isEqualTo(new JSONObject(caseData).toString());
    }

    private MvcResult makePostRequest(String url, Map<String, Object> body) throws Exception {
        return mockMvc
            .perform(post(url)
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
