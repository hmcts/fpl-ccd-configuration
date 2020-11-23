package uk.gov.hmcts.reform.fpl.service.removeorder;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;

import java.util.Map;
import java.util.UUID;

public interface OrderRemovalAction {
    boolean isAccepted(RemovableOrder removableOrder);

    void populateCaseFields(CaseData caseData, Map<String, Object> data, UUID removedOrderId,
                            RemovableOrder removableOrder);

    void action(CaseData caseData, Map<String, Object> data, UUID removedOrderId,
                RemovableOrder removableOrder);
}
