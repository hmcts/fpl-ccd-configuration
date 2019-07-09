package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
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
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.migration.MigratedHearing;
import uk.gov.hmcts.reform.fpl.service.HearingMigrationService;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(HearingController.class)
@OverrideAutoConfiguration(enabled = true)
public class HearingMidEventControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ERROR_MESSAGE = "Date of birth cannot be in the future";

    @Autowired
    private MapperService mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HearingMigrationService mockHearingMigrationService;

    @BeforeEach
    public void setUp() {
        given(mockHearingMigrationService.setMigratedValue(any(CaseDetails.class))).willCallRealMethod();
    }

    @Test
    public void shouldValidateMigrateHearing_whenCaseDataContainsFieldHearing1() throws Exception {
        MigratedHearing migratedHearing = createMigratedHearing();

        HashMap<String, Object> map = MAPPER.readValue(MAPPER.writeValueAsString(migratedHearing),
            new TypeReference<Map<String, Object>>() {
            });

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .lockedBy(32)
                .data(ImmutableMap.<String, Object>builder().put("hearing1", map).build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getErrors()).isEmpty();
        assertThat(callbackResponse.getData()).isNotNull();

        Set<String> actualDataKeyNames = callbackResponse.getData().keySet();
        assertThat(actualDataKeyNames).contains("hearing1");
        assertThat(actualDataKeyNames).doesNotContain("hearing");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldValidateHearing_whenCaseDataContainsFieldHearing() throws Exception {
        Hearing hearing = createHearing();

        HashMap<String, Object> map = MAPPER.readValue(MAPPER.writeValueAsString(hearing),
            new TypeReference<Map<String, Object>>() {
            });

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .lockedBy(32)
                .data(ImmutableMap.<String, Object>builder().put("hearing", map).build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getErrors()).isEmpty();
        assertThat(callbackResponse.getData()).isNotNull();

        Set<String> actualDataKeyNames = callbackResponse.getData().keySet();
        assertThat(actualDataKeyNames).contains("hearing");
        assertThat(actualDataKeyNames).doesNotContain("hearing1");

        // no hearing1

        Map<String, Object> actualHearingData = (Map<String, Object>) callbackResponse.getData().get("hearing");
        assertThat(actualHearingData).isNotNull();

        Hearing actualHearing = mapper.mapObject(actualHearingData, Hearing.class);
        assertThat(actualHearing.getHearingDescription()).isEqualTo("this is a hearing description");
    }


    private Hearing createHearing() {
        return Hearing.builder().hearingDescription("this is a hearing description").build();
    }

    private MigratedHearing createMigratedHearing() {
        return MigratedHearing.builder().hearingDescription("this is a hearing description").build();
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(Hearing hearing) throws Exception {
        HashMap<String, Object> map = MAPPER.readValue(MAPPER.writeValueAsString(hearing),
            new TypeReference<Map<String, Object>>() {
            });

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.<String, Object>builder().put("hearing", map).build())
                .build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/enter-hearing/mid-event")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(CallbackRequest request) throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/enter-hearing/mid-event")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}
