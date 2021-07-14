package uk.gov.hmcts.reform.fpl.service.orders.amendment.list;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
public class AmendableUrgentHearingOrderProvider implements AmendableListItemProvider {
    @Override
    public List<Element<? extends AmendableOrder>> provideListItems(CaseData caseData) {
        UrgentHearingOrder uho = caseData.getUrgentHearingOrder();

        return null != uho ? List.of(element(UrgentHearingOrder.COLLECTION_ID, uho)) : List.of();
    }
}
