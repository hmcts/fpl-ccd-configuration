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
@WebMvcTest(AllocationDecisionController.class)
@OverrideAutoConfiguration(enabled = true)
class AllocationDecisionControllerAboutToStartTest extends AbstractControllerTest {

    AllocationDecisionControllerAboutToStartTest() {
        super("allocation-decision");
    }

    @Test
    void shouldPopulateAllocationDecision() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("allocationDecision");
    }
}
