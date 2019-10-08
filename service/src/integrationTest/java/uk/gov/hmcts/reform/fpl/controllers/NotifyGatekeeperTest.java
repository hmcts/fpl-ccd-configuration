package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.handlers.PopulateStandardDirectionsHandler;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ActiveProfiles("integration-test")
@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
class NotifyGatekeeperTest {

    private static final String TOKEN = "1";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String GATEKEEPER_EMAIL = "FamilyPublicLaw+gatekeeper@gmail.com";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private PopulateStandardDirectionsHandler populateStandardDirectionsHandler;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldReturnPopulatedDirectionsByRoleInSubmittedCallback() throws Exception {
        mockMvc
            .perform(post("/callback/notify-gatekeeper/submitted")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(callbackRequest())))
            .andExpect(status().isOk())
            .andReturn();

        verify(populateStandardDirectionsHandler, times(1)).populateStandardDirections(
            any(PopulateStandardDirectionsEvent.class));
    }

    @Test
    void shouldBuildGatekeeperNotificationTemplateWithCompleteValues() throws Exception {
        List<String> ordersAndDirections = ImmutableList.of("Emergency protection order",
            "Contact with any named person");
        Map<String, Object> expectedGatekeeperParameters = ImmutableMap.<String, Object>builder()
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("ordersAndDirections", ordersAndDirections)
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "Same day")
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        mockMvc
            .perform(post("/callback/notify-gatekeeper/submitted")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("core-case-data-store-api/callback-request.json")))
            .andExpect(status().isOk());

        verify(notificationClient, times(1)).sendEmail(GATEKEEPER_SUBMISSION_TEMPLATE, GATEKEEPER_EMAIL,
            expectedGatekeeperParameters, "12345");
    }
}
