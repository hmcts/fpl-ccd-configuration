package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Allocation;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(AllocationDecisionController.class)
@OverrideAutoConfiguration(enabled = true)
class AllocationDecisionControllerAboutToSubmitTest extends AbstractControllerTest {

    AllocationDecisionControllerAboutToSubmitTest() {
        super("allocation-decision");
    }

    @Test
    void shouldPopulateAllocationDecisionWhenSubmitting() {
        Allocation allocationDecision = createAllocation("Lay justices", "Reason");

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("allocationDecision", allocationDecision))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("allocationDecision");
    }

    private Allocation createAllocation(String proposal, String judgeLevelRadio) {
        Allocation allocationDecision = Allocation.builder()
            .proposal(proposal)
            .judgeLevelRadio(judgeLevelRadio)
            .build();
        return allocationDecision;
    }
}
