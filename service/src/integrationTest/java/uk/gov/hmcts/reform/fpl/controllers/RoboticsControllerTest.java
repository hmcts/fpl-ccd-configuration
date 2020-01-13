package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.EmailService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ActiveProfiles("integration-test")
@WebMvcTest(RoboticsController.class)
@OverrideAutoConfiguration(enabled = true)
public class RoboticsControllerTest extends AbstractControllerTest {
    private static final String CASE_ID = "12345";
    private static final String EMAIL_FROM = "sender@example.com";

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

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgumentCaptor;

    RoboticsControllerTest() {
        super("/sendRPAEmailByID/");
    }

    @BeforeEach
    void setup() {
        given(idamClient.authenticateUser(userConfiguration.getUserName(), userConfiguration.getPassword()))
            .willReturn(userAuthToken);

        given(idamClient.getUserDetails(userAuthToken))
            .willReturn(UserDetails.builder()
                .id(userId)
                .build());

        given(authTokenGenerator.generate())
            .willReturn(serviceAuthToken);
    }

    @Test
    @WithMockUser(authorities = "caseworker-publiclaw-systemupdate")
    void resendCaseDataNotificationShouldResendNotificationWithNoError() throws Exception {
        given(coreCaseDataApi.getCase(userAuthToken, serviceAuthToken, CASE_ID))
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
        given(coreCaseDataApi.getCase(userAuthToken, serviceAuthToken, CASE_ID))
            .willReturn(populatedCaseDetails());

        assertThat(postToUrl(String.format("%s", "1111111111")).getResponse().getStatus())
            .isEqualTo(SC_OK);

        verify(emailService, never()).sendEmail(eq(EMAIL_FROM), emailDataArgumentCaptor.capture());
    }
}
