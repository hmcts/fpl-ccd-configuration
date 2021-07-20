package uk.gov.hmcts.reform.fpl.service.orders.amendment.action;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendStandardDirectionOrderAction implements AmendOrderAction {
    private static final String CASE_FIELD = "standardDirectionOrder";

    private final Time time;

    @Override
    public boolean accept(CaseData caseData) {
        UUID selectedOrderId = caseData.getManageOrdersEventData().getManageOrdersAmendmentList().getValueCodeAsUUID();
        return StandardDirectionOrder.COLLECTION_ID.equals(selectedOrderId);
    }

    @Override
    public Map<String, Object> applyAmendedOrder(CaseData caseData, DocumentReference amendedDocument,
                                                 List<Element<Other>> selectedOthers) {
        StandardDirectionOrder sdo = caseData.getStandardDirectionOrder().toBuilder()
            .amendedDate(time.now().toLocalDate())
            .orderDoc(amendedDocument)
            .others(selectedOthers)
            .build();

        return Map.of(CASE_FIELD, sdo);
    }
}
