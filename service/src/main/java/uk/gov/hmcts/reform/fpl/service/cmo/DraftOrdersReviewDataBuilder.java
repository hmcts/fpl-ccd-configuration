package uk.gov.hmcts.reform.fpl.service.cmo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;

@Component
public class DraftOrdersReviewDataBuilder {

    public Map<String, Object> buildDraftOrdersReviewData(HearingOrdersBundle ordersBundle) {
        Map<String, Object> data = new HashMap<>();

        List<String> draftOrdersTitles = new ArrayList<>();
        data.put("draftCMOExists", "N");

        int counter = 1;
        for (Element<HearingOrder> orderElement : ordersBundle.getOrders(SEND_TO_JUDGE)) {

            if (orderElement.getValue().getType().isCmo()) {
                draftOrdersTitles.add(String.format("CMO%s", ordersBundle.getHearingId() != null
                    ? " for " + ordersBundle.getHearingName() : EMPTY));
                data.put("cmoDraftOrderTitle", orderElement.getValue().getTitle());
                data.put("cmoDraftOrderDocument", orderElement.getValue().getOrder());
                data.put("draftCMOExists", "Y");
            } else {
                draftOrdersTitles.add(String.format("C21 Order%s", ordersBundle.getHearingId() != null
                    ? " - " + ordersBundle.getHearingName() : EMPTY));
                data.put(String.format("draftOrder%dTitle", counter), orderElement.getValue().getTitle());
                data.put(String.format("draftOrder%dDocument", counter), orderElement.getValue().getOrder());
                counter++;
            }
        }
        data.put("draftOrdersTitlesInBundle", String.join("\n", draftOrdersTitles));
        if (counter > 1) {
            String numOfDraftOrders = IntStream.range(1, counter)
                .mapToObj(String::valueOf).collect(Collectors.joining(""));
            data.put("draftBlankOrdersCount", numOfDraftOrders);
        }
        return data;
    }
}
