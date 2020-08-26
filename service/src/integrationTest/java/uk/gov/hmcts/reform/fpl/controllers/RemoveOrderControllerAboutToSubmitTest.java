package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
public class RemoveOrderControllerAboutToSubmitTest extends AbstractControllerTest {
    private static final String REASON = "The order was removed because the order was removed";

    private Element<GeneratedOrder> selectedOrder;

    RemoveOrderControllerAboutToSubmitTest() {
        super("remove-order");
    }

    @BeforeEach
    void initialise() {
        selectedOrder = element(buildOrder());
    }

    @Test
    void shouldUpdateOrderCollectionAndHiddenOrderCollection() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
            asCaseDetails(buildCaseData(selectedOrder))
        );

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        selectedOrder.getValue().setRemovalReason(REASON);

        assertThat(responseData.getOrderCollection()).isEmpty();
        assertThat(responseData.getHiddenOrders()).hasSize(1).containsOnly(selectedOrder);
    }

    @Test
    void shouldRemoveTemporaryFields() {
        CaseData caseData = buildCaseData(selectedOrder);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        assertThat(response.getData()).doesNotContainKeys("removableOrderList", "reasonToRemoveOrder");
    }

    private CaseData buildCaseData(Element<GeneratedOrder> order) {
        return CaseData.builder()
            .orderCollection(List.of(order))
            .removableOrderList(buildRemovableOrderList(order.getId()))
            .reasonToRemoveOrder(REASON)
            .build();
    }

    private DynamicList buildRemovableOrderList(UUID id) {
        return DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(id)
                .label("order - 12 March 1234")
                .build())
            .listItems(List.of(DynamicListElement.builder()
                .code(id)
                .label("order - 12 March 1234")
                .build()))
            .build();
    }

    private GeneratedOrder buildOrder() {
        return GeneratedOrder.builder()
            .type("Blank order (C21)")
            .title("order")
            .dateOfIssue("12 March 1234")
            .build();
    }
}
