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

    @SuppressWarnings("unchecked")
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

        Map<String, Object> actualMigratedHearingData =
            (Map<String, Object>) callbackResponse.getData().get("hearing1");
        assertThat(actualMigratedHearingData).isNotNull();

        MigratedHearing actualMigratedHearing = mapper.mapObject(actualMigratedHearingData, MigratedHearing.class);
        assertThat(actualMigratedHearing.getHearingDescription()).isEqualTo("this is a migrated hearing description");
        assertThat(actualMigratedHearing.getReason()).isEqualTo("migrated hearing reason");
        assertThat(actualMigratedHearing.getTimeFrame()).isEqualTo("migrated hearing timeframe");
        assertThat(actualMigratedHearing.getSameDayHearingReason())
            .isEqualTo("migrated hearing same day hearing reason");
        assertThat(actualMigratedHearing.getWithoutNotice()).isEqualTo("migrated hearing without notice");
        assertThat(actualMigratedHearing.getReasonForNoNotice()).isEqualTo("migrated hearing reason for no notice");
        assertThat(actualMigratedHearing.getReducedNotice()).isEqualTo("migrated hearing reduced notice");
        assertThat(actualMigratedHearing.getReasonForReducedNotice())
            .isEqualTo("migrated hearing reason for reduced notice");
        assertThat(actualMigratedHearing.getRespondentsAware()).isEqualTo("migrated hearing respondants aware");
        assertThat(actualMigratedHearing.getReasonsForRespondentsNotBeingAware())
            .isEqualTo("migrated hearing reasons for respondants not being aware");
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

        Map<String, Object> actualHearingData = (Map<String, Object>) callbackResponse.getData().get("hearing");
        assertThat(actualHearingData).isNotNull();

        Hearing actualHearing = mapper.mapObject(actualHearingData, Hearing.class);
        assertThat(actualHearing.getDescription()).isEqualTo("this is a hearing description");
        assertThat(actualHearing.getReason()).isEqualTo("hearing reason");
        assertThat(actualHearing.getTimeFrame()).isEqualTo("hearing timeframe");
        assertThat(actualHearing.getSameDayHearingReason()).isEqualTo("hearing same day hearing reason");
        assertThat(actualHearing.getWithoutNotice()).isEqualTo("hearing without notice");
        assertThat(actualHearing.getReasonForNoNotice()).isEqualTo("hearing reason for no notice");
        assertThat(actualHearing.getReducedNotice()).isEqualTo("hearing reduced notice");
        assertThat(actualHearing.getReasonForReducedNotice()).isEqualTo("hearing reason for reduced notice");
        assertThat(actualHearing.getRespondentsAware()).isEqualTo("hearing respondants aware");
        assertThat(actualHearing.getReasonsForRespondentsNotBeingAware())
            .isEqualTo("hearing reasons for respondants not being aware");
        //assertThat(actualHearing.getCreatedBy()).isEqualTo("32");
        assertThat(actualHearing.getCreatedDate()).isEqualTo("09-07-2019");
    }

    private Hearing createHearing() {
        return Hearing.builder()
            .description("this is a hearing description")
            .reason("hearing reason")
            .timeFrame("hearing timeframe")
            .sameDayHearingReason("hearing same day hearing reason")
            .withoutNotice("hearing without notice")
            .reasonForNoNotice("hearing reason for no notice")
            .reducedNotice("hearing reduced notice")
            .reasonForReducedNotice("hearing reason for reduced notice")
            .respondentsAware("hearing respondants aware")
            .reasonsForRespondentsNotBeingAware("hearing reasons for respondants not being aware")
            .createdBy("12")
            .createdDate("09-07-2019")
            .build();
    }

    private MigratedHearing createMigratedHearing() {
        return MigratedHearing.builder()
            .hearingDescription("this is a migrated hearing description")
            .reason("migrated hearing reason")
            .timeFrame("migrated hearing timeframe")
            .sameDayHearingReason("migrated hearing same day hearing reason")
            .withoutNotice("migrated hearing without notice")
            .reasonForNoNotice("migrated hearing reason for no notice")
            .reducedNotice("migrated hearing reduced notice")
            .reasonForReducedNotice("migrated hearing reason for reduced notice")
            .respondentsAware("migrated hearing respondants aware")
            .reasonsForRespondentsNotBeingAware("migrated hearing reasons for respondants not being aware")
            .build();
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
