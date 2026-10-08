package uk.gov.hmcts.reform.fpl.utils;

import lombok.experimental.UtilityClass;
import uk.gov.hmcts.reform.fpl.model.ConfidentialOrderBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@UtilityClass
public class ConfidentialOrderBundleUtils {

    public static <T> Map<String, List<Element<T>>> addToConfidentialOrderBundle(
        Element<HearingOrdersBundle> draftOrdersBundle,
        Element<HearingOrder> draftOrderElement,
        ConfidentialOrderBundle<T> confidentialOrderBundle,
        Element<T> orderToBeAdded
    ) {
        Map<String, List<Element<T>>> updates = new HashMap<>();

        draftOrdersBundle.getValue().processAllConfidentialOrders((suffix, selectedDraftOrders) -> {
            if (isNotEmpty(selectedDraftOrders)
                && ElementUtils.findElement(draftOrderElement.getId(), selectedDraftOrders).isPresent()) {
                List<Element<T>> confidentialOrders =
                    defaultIfNull(confidentialOrderBundle.getConfidentialOrdersBySuffix(suffix), new ArrayList<>());
                confidentialOrders.add(orderToBeAdded);
                updates.put(confidentialOrderBundle.getFieldBaseName() + suffix, confidentialOrders);
                confidentialOrderBundle.setConfidentialOrdersBySuffix(suffix, confidentialOrders);
            }
        });

        return updates;
    }
}

