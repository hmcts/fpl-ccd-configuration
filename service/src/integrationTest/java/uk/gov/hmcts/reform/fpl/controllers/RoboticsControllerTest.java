package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.service.EmailService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ActiveProfiles("integration-test")
@WebMvcTest(RoboticsController.class)
@OverrideAutoConfiguration(enabled = true)
public class RoboticsControllerTest {
    private static final String CASE_ID = "12345";
    private static final String USER_AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-token";

    @MockBean
    private SystemUpdateUserConfiguration userConfiguration;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        given(idamClient.authenticateUser(userConfiguration.getUserName(), userConfiguration.getPassword()))
            .willReturn(USER_AUTH_TOKEN);

        given(idamClient.getUserDetails(USER_AUTH_TOKEN))
            .willReturn(UserDetails.builder()
                .id(USER_ID)
                .build());

        given(authTokenGenerator.generate())
            .willReturn(SERVICE_AUTH_TOKEN);
    }

    @Test
    @WithMockUser(authorities = "caseworker-publiclaw-systemupdate")
    void resendCaseDataNotificationShouldResendNotificationWithNoError() throws Exception {
        given(coreCaseDataApi.getCase(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID))
            .willReturn(populatedCaseDetails());

        assertThat(postToUrl(String.format("%s", CASE_ID)).getResponse().getStatus())
            .isEqualTo(SC_OK);
    }

    @Test
    @WithMockUser(authorities = "caseworker-publiclaw-judiciary")
    void resendCaseDataNotificationShouldThrowForbiddenErrorWhenJudiciaryRole() throws Exception {
        assertThat(postToUrl(String.format("%s", CASE_ID)).getResponse().getStatus())
            .isEqualTo(FORBIDDEN.value());
    }

    @Test
    @WithMockUser(authorities = "caseworker-publiclaw-systemupdate")
    void resendCaseDataNotificationShouldNotResendNotificationWhenWrongCaseIdSentInRequest() throws Exception {
        assertThat(postToUrl("1111111111").getResponse().getStatus())
            .isEqualTo(SC_OK);

        verify(emailService, never()).sendEmail(any(), any());
    }

    private MvcResult postToUrl(final String caseId) throws Exception {
        return mockMvc
            .perform(post(String.format("/sendRPAEmailByID/%s", caseId))
                .header("authorization", USER_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }
}
