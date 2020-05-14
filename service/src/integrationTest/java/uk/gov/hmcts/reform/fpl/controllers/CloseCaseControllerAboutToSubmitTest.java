package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason.DEPRIVATION_OF_LIBERTY;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason.WITHDRAWN;

@ActiveProfiles("integration-test")
@WebMvcTest(CloseCaseController.class)
@OverrideAutoConfiguration(enabled = true)
public class CloseCaseControllerAboutToSubmitTest extends AbstractControllerTest {

    CloseCaseControllerAboutToSubmitTest() {
        super("close-case");
    }

    @Test
    void shouldSetDeprivationOfLibertyFlagToYesWhenDeprivationReasonIsSet() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "closeCase", Map.of("fullReason", DEPRIVATION_OF_LIBERTY)
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).containsEntry("deprivationOfLiberty", "Yes");
    }

    @Test
    void shouldSetDeprivationOfLibertyFlagToNoWhenDeprivationReasonIsNotSet() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "closeCase", Map.of("fullReason", WITHDRAWN)
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).containsEntry("deprivationOfLiberty", "No");
    }

}
