package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractTest;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;

import java.time.LocalDate;

import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@WebMvcTest(RoboticsController.class)
@OverrideAutoConfiguration(enabled = true)
class RoboticsControllerTest extends AbstractTest {
    private static final String CASE_ID = "12345";

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "caseworker-publiclaw-systemupdate")
    void resendCaseDataNotificationShouldResendNotificationWithNoError() throws Exception {
        givenFplService();

        given(coreCaseDataApi.getCase(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID))
            .willReturn(expectedCaseDetailsWithState("Submitted"));

        assertThat(postToUrl(CASE_ID).getResponse().getStatus())
            .isEqualTo(SC_OK);

        verify(emailService).sendEmail(any(), any());
    }

    @Test
    @WithMockUser(authorities = "caseworker-publiclaw-judiciary")
    void resendCaseDataNotificationShouldThrowForbiddenErrorWhenJudiciaryRole() throws Exception {
        assertThat(postToUrl(CASE_ID).getResponse().getStatus())
            .isEqualTo(FORBIDDEN.value());

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    @WithMockUser(authorities = "caseworker-publiclaw-systemupdate")
    void resendCaseDataNotificationShouldNotResendNotificationWhenWrongCaseIdSentInRequest() throws Exception {
        assertThat(postToUrl("1111111111").getResponse().getStatus())
            .isEqualTo(NOT_FOUND.value());

        verify(emailService, never()).sendEmail(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Open", "Deleted"})
    @WithMockUser(authorities = "caseworker-publiclaw-systemupdate")
    void resendCaseDataNotificationShouldNotResendNotificationWhenCaseFoundInExcludedState(final String state)
        throws Exception {

        givenFplService();

        given(coreCaseDataApi.getCase(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID))
            .willReturn(expectedCaseDetailsWithState(state));

        assertThat(postToUrl(CASE_ID).getResponse().getStatus())
            .isEqualTo(BAD_REQUEST.value());

        verify(emailService, never()).sendEmail(any(), any());
    }

    private CaseDetails expectedCaseDetailsWithState(final String state) {
        CaseDetails caseDetails = populatedCaseDetails();
        caseDetails.setState(state);
        caseDetails.getData().put("dateSubmitted", LocalDate.now());
        return caseDetails;
    }

    private MvcResult postToUrl(final String caseId) throws Exception {
        return mockMvc
            .perform(post(String.format("/sendRPAEmailByID/%s", caseId))
                .header("authorization", USER_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }
}
