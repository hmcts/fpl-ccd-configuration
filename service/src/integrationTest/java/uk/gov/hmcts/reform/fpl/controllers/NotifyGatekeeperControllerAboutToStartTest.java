package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

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
    void shouldResetGateKeeperEmailCollection() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails(
            VALID_FAMILY_MAN_NUMBER, SUBMITTED));

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        List<Element<EmailAddress>> gateKeeperEmailAddresses = caseData.getGateKeeperEmails();

        assertThat(gateKeeperEmailAddresses.size()).isEqualTo(1);
        assertThat(gateKeeperEmailAddresses.get(0).getValue().getEmail()).isEqualTo("");
    }

    private CaseDetails caseDetails(String familyManNumber, String state) {
        return CaseDetails.builder()
            .id(12345L)
            .data(Map.of(
                "familyManCaseNumber", familyManNumber,
                "gateKeeperEmails", wrapElements(
                        element(UUID.randomUUID(), EmailAddress.builder().email("test1@gmail.com").build()),
                        element(UUID.randomUUID(), EmailAddress.builder().email("test2@gmail.com").build()))
            ))
            .state(state)
            .build();
    }
}
