package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(OrdersNeededAboutToSubmitCallbackController.class)
@OverrideAutoConfiguration(enabled = true)
class OrdersNeededAboutToSubmitCallbackControllerTest extends AbstractControllerTest {

    OrdersNeededAboutToSubmitCallbackControllerTest() {
        super("orders-needed");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldAddEPOReasoningShowValueToCaseDataWhenCallbackContainsEPO() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent("fixtures/case.json");

        assertThat((List<String>) response.getData().get("EPO_REASONING_SHOW")).contains("SHOW_FIELD");
    }

    @Test
    void shouldRemoveGroundsForEPODataWhenEPOIsUnselected() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent("fixtures/caseDataWithRemovedEPO.json");

        assertThat(response.getData().get("groundsForEPO")).isEqualTo(null);
        assertThat(response.getData().get("EPO_REASONING_SHOW")).isEqualTo(null);
    }
}
