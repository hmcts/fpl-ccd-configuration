package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.ConfidentialRefusedOrders;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class ConfidentialOrderBundleUtilsTest {

    @Test
    void shouldAddOrderToMatchingConfidentialOrderCollection() {
        UUID draftOrderId = UUID.randomUUID();
        Element<HearingOrder> draftOrderElement = element(draftOrderId, HearingOrder.builder().title("draft").build());
        Element<HearingOrder> orderToBeAdded = element(HearingOrder.builder().title("returned").build());

        Element<HearingOrdersBundle> draftOrdersBundle = element(
            HearingOrdersBundle.builder()
                .ordersCTSC(List.of(draftOrderElement))
                .build()
        );

        ConfidentialRefusedOrders confidentialRefusedOrders = ConfidentialRefusedOrders.builder().build();

        Map<String, List<Element<HearingOrder>>> updates = ConfidentialOrderBundleUtils.addToConfidentialOrderBundle(
            draftOrdersBundle,
            draftOrderElement,
            confidentialRefusedOrders,
            orderToBeAdded
        );

        assertThat(updates)
            .containsOnlyKeys("refusedHearingOrdersCTSC")
            .extractingByKey("refusedHearingOrdersCTSC")
            .asList()
            .containsExactly(orderToBeAdded);

        assertThat(confidentialRefusedOrders.getRefusedHearingOrdersCTSC())
            .containsExactly(orderToBeAdded);
    }

    @Test
    void shouldAppendOrderToExistingConfidentialOrderCollection() {
        UUID draftOrderId = UUID.randomUUID();
        Element<HearingOrder> draftOrderElement = element(draftOrderId, HearingOrder.builder().title("draft").build());
        Element<HearingOrder> existingOrder = element(HearingOrder.builder().title("existing").build());
        Element<HearingOrder> orderToBeAdded = element(HearingOrder.builder().title("new").build());

        Element<HearingOrdersBundle> draftOrdersBundle = element(
            HearingOrdersBundle.builder()
                .ordersResp0(List.of(draftOrderElement))
                .build()
        );

        ConfidentialRefusedOrders confidentialRefusedOrders = ConfidentialRefusedOrders.builder()
            .refusedHearingOrdersResp0(new ArrayList<>(List.of(existingOrder)))
            .build();

        Map<String, List<Element<HearingOrder>>> updates = ConfidentialOrderBundleUtils.addToConfidentialOrderBundle(
            draftOrdersBundle,
            draftOrderElement,
            confidentialRefusedOrders,
            orderToBeAdded
        );

        assertThat(updates.get("refusedHearingOrdersResp0"))
            .containsExactly(existingOrder, orderToBeAdded);
        assertThat(confidentialRefusedOrders.getRefusedHearingOrdersResp0())
            .containsExactly(existingOrder, orderToBeAdded);
    }

    @Test
    void shouldNotUpdateWhenDraftOrderIsNotInAnyConfidentialCollection() {
        UUID draftOrderId = UUID.randomUUID();
        Element<HearingOrder> draftOrderElement = element(draftOrderId, HearingOrder.builder().title("draft").build());
        Element<HearingOrder> differentDraftOrder = element(UUID.randomUUID(), HearingOrder.builder().title("other").build());

        Element<HearingOrdersBundle> draftOrdersBundle = element(
            HearingOrdersBundle.builder()
                .ordersLA(List.of(differentDraftOrder))
                .build()
        );

        ConfidentialRefusedOrders confidentialRefusedOrders = ConfidentialRefusedOrders.builder().build();

        Map<String, List<Element<HearingOrder>>> updates = ConfidentialOrderBundleUtils.addToConfidentialOrderBundle(
            draftOrdersBundle,
            draftOrderElement,
            confidentialRefusedOrders,
            element(HearingOrder.builder().title("new").build())
        );

        assertThat(updates).isEmpty();
        assertThat(confidentialRefusedOrders.getRefusedHearingOrdersLA()).isNull();
    }
}

