package uk.gov.hmcts.reform.fpl.service.removeorder;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.UUID;

public interface OrderRemovalAction {
    boolean isAccepted(RemovableOrder removableOrder);

    void populateCaseFields(CaseData caseData, CaseDetailsMap data, UUID removedOrderId,
                            RemovableOrder removableOrder);

    void remove(CaseData caseData, CaseDetailsMap data, UUID removedOrderId,
                RemovableOrder removableOrder);
}
