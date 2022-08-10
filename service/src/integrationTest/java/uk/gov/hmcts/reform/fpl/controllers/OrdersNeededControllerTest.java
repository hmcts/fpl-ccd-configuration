package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(OrdersNeededController.class)
@OverrideAutoConfiguration(enabled = true)
class OrdersNeededControllerTest extends AbstractCallbackTest {

    OrdersNeededControllerTest() {
        super("orders-needed");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldAddEPOReasoningShowValueToCaseDataWhenCallbackContainsEPO() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent("fixtures/case.json");
        assertThat(response.getData().get("otherOrderType")).isEqualTo("NO");
        assertThat((List<String>) response.getData().get("EPO_REASONING_SHOW")).contains("SHOW_FIELD");
    }

    @Test
    void shouldRemoveGroundsForEPODataWhenEPOIsUnselected() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent("fixtures/caseDataWithRemovedEPO.json");

        assertThat(response.getData().get("groundsForEPO")).isEqualTo(null);
        assertThat(response.getData().get("EPO_REASONING_SHOW")).isEqualTo(null);
    }

    @Test
    void shouldSetOtherOrderTypeToYesWhenOtherOrderTypeIsSelected() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent("fixtures/caseOtherOrderType.json");

        assertThat(response.getData().get("otherOrderType")).isEqualTo("YES");
        assertThat(response.getData().get("EPO_REASONING_SHOW")).isEqualTo(null);
    }

    @Test
    void shouldRaiseErrorWhenStandaloneAndCareOrderSelected() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            "fixtures/caseCareAndStandaloneOrderType.json");

        assertThat(response.getErrors()).contains("You have selected a standalone order, "
            + "this cannot be applied for alongside other orders.");
    }

    @Test
    void shouldSetCourtWhenCourtIsSelected() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent("fixtures/caseOtherOrderType.json");

        assertThat(response.getData().get("court")).isNotNull();
    }

    @Test
    void shouldSetOrdersToOrdersSolicitorForSolicitorsAfterOrderSubmitted() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent("fixtures/caseRespondentSolicitor.json");

        assertThat(response.getData().get("representativeType")).isEqualTo("RESPONDENT_SOLICITOR");
        assertThat(response.getData().get("orders")).isEqualTo(response.getData().get("ordersSolicitor"));
    }

    @Test
    void shouldSetRepresentativeTypeToLAIfItIsNotAlreadySet() {
        //In this case fixture, representativeType is not set.
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent("fixtures/caseOtherOrderType.json");

        assertThat(response.getData().get("representativeType")).isEqualTo("LOCAL_AUTHORITY");
    }
}
