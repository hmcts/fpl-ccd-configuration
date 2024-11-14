package uk.gov.hmcts.reform.fpl.service.orders.amendment.list;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
public class AmendableStandardDirectionOrderProvider implements AmendableListItemProvider {
    @Override
    public List<Element<? extends AmendableOrder>> provideListItems(CaseData caseData) {
        StandardDirectionOrder sdo = caseData.getStandardDirectionOrder();

        return null != sdo && sdo.isSealed()
            ? List.of(element(StandardDirectionOrder.SDO_COLLECTION_ID, sdo)) : List.of();
    }
}
