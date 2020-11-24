package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;

import java.util.Map;
import java.util.UUID;

@Component
public class SDOOrderRemovalAction implements OrderRemovalAction {

    @Override
    public boolean isAccepted(RemovableOrder removableOrder) {
        return removableOrder instanceof StandardDirectionOrder;
    }

    @Override
    public void action(CaseData caseData, Map<String, Object> data, UUID removedOrderId,
                       RemovableOrder removableOrder) {

        StandardDirectionOrder standardDirectionOrder = (StandardDirectionOrder) removableOrder;

        standardDirectionOrder.setRemovalReason(caseData.getReasonToRemoveOrder());

        data.put("standardDirectionOrder",standardDirectionOrder);
        data.put("state", State.GATEKEEPING);
        data.remove("noticeOfProceedings");
    }
}
