package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GroundsForRefuseContactWithChild;
import uk.gov.hmcts.reform.fpl.model.Orders;

import java.util.List;
import java.util.Map;

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
    void shouldShowSecureAccommodationOrderFieldWhenSecureAccommodationOrderIsSelected() {
        AboutToStartOrSubmitCallbackResponse response =
            postAboutToSubmitEvent("fixtures/caseSecureAccommodationOrder.json");

        assertThat(response.getData().get("secureAccommodationOrderType")).isEqualTo("YES");
        assertThat(response.getData().get("otherOrderType")).isEqualTo("NO");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRemoveSecureAccommodationOrderDataWhenSecureAccommodationOrderIsUnselected() {
        AboutToStartOrSubmitCallbackResponse response =
            postAboutToSubmitEvent("fixtures/caseWithSecureAccommodationOrderRemoved.json");

        assertThat(response.getData().get("secureAccommodationOrderType")).isEqualTo("NO");
        assertThat(((Map<String, Object>) response.getData().get("orders")).get("secureAccommodationOrderSection"))
            .isEqualTo(null);
    }

    @Test
    void shouldShowRefuseContactFieldWhenRefuseContactIsSelected() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(CaseData.builder()
            .orders(Orders.builder().orderType(List.of(OrderType.REFUSE_CONTACT_WITH_CHILD)).build()).build());

        assertThat(response.getData().get("refuseContactWithChildOrderType")).isEqualTo("YES");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRemoveRefuseContactDataWhenRefuseContactIsUnselected() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(CaseData.builder()
            .orders(Orders.builder().orderType(List.of(OrderType.CARE_ORDER)).build())
            .groundsForRefuseContactWithChild(GroundsForRefuseContactWithChild.builder()
                .personHasContactAndCurrentArrangement("test").build()).build());

        assertThat(response.getData().get("refuseContactWithChildOrderType")).isNull();
        assertThat((Map<String, Object>) response.getData().get("groundsForRefuseContactWithChild")).isNullOrEmpty();
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

    @Test
    @SuppressWarnings("unchecked")
    void shouldSetCaseManagementLocation() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent("fixtures/case.json");
        //assertThat(response.getData().get("caseManagementLocation")).isEqualTo("NO");

        // court code (344) is defined by application-integration-test.yaml (by LOCAL_AUTHORITY_4_USER_EMAIL)
        // epimms id is defined in courts.json by looking up court code 344
        @SuppressWarnings("unchecked")
        Map<String, String> caseManagementLocation = (Map<String, String>)
            response.getData().get("caseManagementLocation");
        assertThat(caseManagementLocation).containsEntry("baseLocation", "234946");
        assertThat(caseManagementLocation).containsEntry("region", "7");
    }
}
