package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
class NotifyGatekeeperControllerMidEventTest extends AbstractControllerTest {
    NotifyGatekeeperControllerMidEventTest() {
        super("notify-gatekeeper");
    }

    @Test
    void shouldReturnErrorsWhenGatekeeperEmailsAreInValid() {
        CaseData caseData = CaseData.builder()
            .gatekeeperEmails(List.of(
                element(EmailAddress.builder().email("email@example.com").build()),
                element(EmailAddress.builder().email("<John Doe> johndoe@email.com").build()),
                element(EmailAddress.builder().email("email@example.name").build()),
                element(EmailAddress.builder().email("very.unusual.”@”.unusual.com@example.com").build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).contains(
            "Gatekeeper 2: Enter an email address in the correct format, for example name@example.com",
            "Gatekeeper 4: Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldNotReturnErrorsWhenGatekeeperEmailsAreValid() {
        CaseData caseData = CaseData.builder()
            .gatekeeperEmails(List.of(
                element(EmailAddress.builder().email("email@example.com").build()),
                element(EmailAddress.builder().email("email@example.name").build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenSingleGatekeeperEmailIsInvalid() {
        CaseData caseData = CaseData.builder()
            .state(State.SUBMITTED)
            .gatekeeperEmails(List.of(
                element(EmailAddress.builder().email("<John Doe> johndoe@email.com").build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).contains(
            "Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldNotReturnAnErrorWhenSingleGatekeeperEmailIsValid() {
        CaseData caseData = CaseData.builder()
            .gatekeeperEmails(List.of(
                element(EmailAddress.builder().email("email@example.name").build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).isNull();
    }
}
