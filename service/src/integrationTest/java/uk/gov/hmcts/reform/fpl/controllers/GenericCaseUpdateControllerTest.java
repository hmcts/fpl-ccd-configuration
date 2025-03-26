package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(GenericCaseUpdateController.class)
@OverrideAutoConfiguration(enabled = true)
class GenericCaseUpdateControllerTest extends AbstractTest{
    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2025,3,26,8,0,0,0);
    private static final String EVENT_NAME = "generic-update";
    private static final String USER_AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @MockBean
    private Time time;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        givenFplService();
        given(time.now()).willReturn(TEST_TIME);
        given(featureToggleService.isCafcassApiToggledOn()).willReturn(true);
    }

    @Test
    void shouldUpdateLastGenuineUpdateTimedWhenAffectCafcassApiResponse() {
        Map<String, Object> caseBefore = Map.of("id", 1L,
            "respondents1", wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("firstName").lastName("lastName").build())
                .build()));

        Map<String, Object> caseAfter = Map.of("id", 1L,
            "respondents1", wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("firstName").lastName("updatedLastName").build())
                .build()));

        CallbackRequest request = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder()
                .state(State.SUBMITTED.getValue())
                .data(caseBefore)
                .build())
            .caseDetails(CaseDetails.builder()
                .state(State.SUBMITTED.getValue())
                .data(caseAfter)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToSubmitEvent(request);

        assertThat(actualResponse.getData()).containsOnlyKeys("id", "respondents1", "lastGenuineUpdateTimed");
        assertThat(actualResponse.getData()).extracting( "lastGenuineUpdateTimed").isEqualTo("2025-03-26T08:00:00");

        CaseData caseData = extractCaseData(actualResponse);

        assertThat(caseData.getId()).isEqualTo(1L);
        assertThat(caseData.getRespondents1()).isEqualTo(caseAfter.get("respondents1"));
    }

    @Test
    void shouldNotUpdateLastGenuineUpdateTimedIfNotAffectingCafcassApiResponse() {
        Map<String, Object> caseBefore = Map.of("id", 1L,
            "judicialMessages", wrapElements(JudicialMessage.builder().latestMessage("Hi").build()));

        Map<String, Object> caseAfter = Map.of("id", 1L,
            "judicialMessages", wrapElements(JudicialMessage.builder().latestMessage("Hi").build(),
                JudicialMessage.builder().latestMessage("Hi again").build()));

        CallbackRequest request = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder()
                .state(State.SUBMITTED.getValue())
                .data(caseBefore)
                .build())
            .caseDetails(CaseDetails.builder()
                .state(State.SUBMITTED.getValue())
                .data(caseAfter)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToSubmitEvent(request);
        CaseData caseData = extractCaseData(actualResponse);

        assertThat(actualResponse.getData()).containsOnlyKeys("id", "judicialMessages");
        assertThat(caseData.getId()).isEqualTo(1L);
        assertThat(caseData.getJudicialMessages()).isEqualTo(caseAfter.get("judicialMessages"));
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "OPEN", "DELETED", "RETURNED"
    })
    void shouldNotUpdateLastGenuineUpdateTimedIfCaseStateExcluded(State caseState) {
        Map<String, Object> caseBefore = Map.of("id", 1L,
            "respondents1", wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("firstName").lastName("lastName").build())
                .build()));

        Map<String, Object> caseAfter = Map.of("id", 1L,
            "respondents1", wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("firstName").lastName("updatedLastName").build())
                .build()));

        CallbackRequest request = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder()
                .state(State.CASE_MANAGEMENT.getValue())
                .data(caseBefore)
                .build())
            .caseDetails(CaseDetails.builder()
                .state(caseState.getValue())
                .data(caseAfter)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToSubmitEvent(request);
        CaseData actualCaseData = extractCaseData(actualResponse);

        assertThat(actualResponse.getData()).containsOnlyKeys("id", "respondents1");
        assertThat(actualCaseData.getId()).isEqualTo(1L);
        assertThat(actualCaseData.getRespondents1()).isEqualTo(caseAfter.get("respondents1"));
    }

    private AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CallbackRequest request) {
        return postEvent(String.format("/callback/%s/about-to-submit", EVENT_NAME), toBytes(request), SC_OK);
    }

    @SuppressWarnings("unchecked")
    private <T> T postEvent(String path, byte[] data, int expectedStatus, String... userRoles) {
        try {
            MvcResult response = mockMvc
                .perform(post(path)
                    .header("authorization", USER_AUTH_TOKEN)
                    .header("user-id", USER_ID)
                    .header("user-roles", String.join(",", userRoles))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(data))
                .andExpect(status().is(expectedStatus))
                .andReturn();

            byte[] responseBody = response.getResponse().getContentAsByteArray();

            if (responseBody.length > 0) {
                return mapper.readValue(responseBody, (Class<T>) AboutToStartOrSubmitCallbackResponse.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] toBytes(Object o) {
        try {
            return mapper.writeValueAsString(o).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private CaseData extractCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return mapper.convertValue(response.getData(), CaseData.class);
    }
}
