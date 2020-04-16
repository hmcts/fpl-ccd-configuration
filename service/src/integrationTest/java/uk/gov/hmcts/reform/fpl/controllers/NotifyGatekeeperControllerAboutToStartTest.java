package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ActiveProfiles("integration-test")
@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
class NotifyGatekeeperControllerAboutToStartTest extends AbstractControllerTest {

    private static final String INVALID_FAMILY_MAN_NUMBER = "";
    private static final String VALID_FAMILY_MAN_NUMBER = "some string";
    private static final String SUBMITTED = "Submitted";
    private static final String GATEKEEPING = "Gatekeeping";

    @SpyBean
    private ValidateGroupService validateGroupService;

    NotifyGatekeeperControllerAboutToStartTest() {
        super("notify-gatekeeper");
    }

    @Test
    void shouldReturnErrorsWhenFamilymanNumberIsNotProvided() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails(
            INVALID_FAMILY_MAN_NUMBER, SUBMITTED));

        assertThat(callbackResponse.getErrors()).containsExactly("Enter Familyman case number");
    }

    @Test
    void shouldNotValidateFamilymanNumberWhenInGatekeepingState() {
        postAboutToStartEvent(caseDetails(INVALID_FAMILY_MAN_NUMBER, GATEKEEPING));

        verify(validateGroupService, never()).validateGroup(any(), any());
    }

    @Test
    void shouldRemoveGatekeeperEmailWhenPresent() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails(VALID_FAMILY_MAN_NUMBER,
            SUBMITTED));

        assertThat(response.getData()).doesNotContainKeys("gateKeeperEmail");
    }

    private CaseDetails caseDetails(String familyManNumber, String state) {
        return CaseDetails.builder()
            .id(12345L)
            .data(Map.of(
                "familyManCaseNumber", familyManNumber,
                "gateKeeperEmail", "send@spam.com"
            ))
            .state(state)
            .build();
    }
}
