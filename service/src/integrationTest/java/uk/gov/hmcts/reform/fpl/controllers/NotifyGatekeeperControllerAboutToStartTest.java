package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
class NotifyGatekeeperControllerAboutToStartTest extends AbstractControllerTest {

    private static final String SUBMITTED = "Submitted";
    private static final String GATEKEEPING = "Gatekeeping";

    NotifyGatekeeperControllerAboutToStartTest() {
        super("notify-gatekeeper");
    }

    @Test
    void shouldReturnErrorsWhenFamilymanNumberIsNotProvided() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails("", SUBMITTED));

        assertThat(callbackResponse.getErrors()).containsExactly("Enter Familyman case number");
    }

    @Test
    void shouldNotValidateFamilymanNumberWhenInGatekeepingState() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails("", GATEKEEPING));

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldRemoveGatekeeperEmailWhenPresent() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails("some string", SUBMITTED));

        assertThat(response.getData()).doesNotContainKeys("gateKeeperEmail");
    }

    private CaseDetails caseDetails(String familyManCaseNumber, String state) {
        return CaseDetails.builder()
            .id(12345L)
            .data(Map.of(
                "familyManCaseNumber", familyManCaseNumber,
                "gateKeeperEmail", "send@spam.com"
                ))
            .state(state)
            .build();
    }
}
