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
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.migration.MigratedHearing;
import uk.gov.hmcts.reform.fpl.service.HearingMigrationService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(HearingController.class)
@OverrideAutoConfiguration(enabled = true)
public class HearingAboutToStartControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String USER_ID = "1";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HearingMigrationService mockHearingMigrationService;

    @BeforeEach
    public void setUp() {
        given(mockHearingMigrationService.setMigratedValue(any(CaseDetails.class))).willCallRealMethod();
    }

    @Test
    void shouldAddHearingMigratedFieldWithValueNo_whenNoHearingIsInCaseData() throws Exception {
        Applicant notHearing = Applicant.builder().build();

        HashMap<String, Object> map = MAPPER.readValue(MAPPER.writeValueAsString(notHearing),
            new TypeReference<Map<String, Object>>() {
            });

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.<String, Object>builder().put("something that isn't a hearing", map).build())
                .build())
            .build();

        MvcResult response = doRequest(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse =
            mapResponse(response.getResponse().getContentAsByteArray());

        assertThat(callbackResponse.getData())
            .containsEntry("hearingMigrated", "Yes");

        verify(mockHearingMigrationService, times(1)).setMigratedValue(any(CaseDetails.class));
    }

    @Test
    void shouldAddHearingMigratedFieldWithValueYes_whenHearing1IsInCaseData() throws Exception {
        MigratedHearing migratedHearing = MigratedHearing.builder().build();

        HashMap<String, Object> map = MAPPER.readValue(MAPPER.writeValueAsString(migratedHearing),
            new TypeReference<Map<String, Object>>() {
            });

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.<String, Object>builder().put("hearing1", map).build())
                .build())
            .build();

        MvcResult response = doRequest(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse =
            mapResponse(response.getResponse().getContentAsByteArray());

        assertThat(callbackResponse.getData())
            .containsEntry("hearingMigrated", "Yes");

        verify(mockHearingMigrationService, times(1)).setMigratedValue(any(CaseDetails.class));
    }

    @Test
    void shouldAddHearingMigratedFieldWithValueNo_whenMigratedHearingIsInCaseData() throws Exception {
        MigratedHearing migratedHearing = MigratedHearing.builder().build();

        HashMap<String, Object> map = MAPPER.readValue(MAPPER.writeValueAsString(migratedHearing),
            new TypeReference<Map<String, Object>>() {
            });

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.<String, Object>builder().put("hearing", map).build())
                .build())
            .build();

        MvcResult response = doRequest(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse =
            mapResponse(response.getResponse().getContentAsByteArray());

        assertThat(callbackResponse.getData())
            .containsEntry("hearingMigrated", "No");

        verify(mockHearingMigrationService, times(1)).setMigratedValue(any(CaseDetails.class));
    }

    private MvcResult doRequest(CallbackRequest request) throws Exception {
        return mockMvc
            .perform(post("/callback/enter-hearing/about-to-start")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andReturn();
    }

    private AboutToStartOrSubmitCallbackResponse mapResponse(byte[] response) throws IOException {
        return MAPPER.readValue(response, AboutToStartOrSubmitCallbackResponse.class);
    }
}
