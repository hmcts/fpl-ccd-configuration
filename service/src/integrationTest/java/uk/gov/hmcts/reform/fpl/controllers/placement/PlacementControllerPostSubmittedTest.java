package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
public class PlacementControllerPostSubmittedTest extends AbstractCallbackTest {

    PlacementControllerPostSubmittedTest()  {
        super("placement/post-submit-callback");
    }

    @Test
    void removeTemporaryFields() {

        CaseDetails caseData = CaseDetails.builder()
                .id(1234123412341234L)
                .state(CASE_MANAGEMENT.getLabel())
                .data(ofEntries(
                        entry("placementIdToBeSealed", "")))
                .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        assertThat(response.getData()).doesNotContainKeys("placement");
        assertThat(response.getData()).doesNotContainKeys("placementIdToBeSealed");
    }
}
