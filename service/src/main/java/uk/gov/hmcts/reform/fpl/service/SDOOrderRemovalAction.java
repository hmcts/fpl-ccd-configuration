package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;

import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;

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
        standardDirectionOrder = standardDirectionOrder.toBuilder().judgeAndLegalAdvisor(null).build();

        data.remove("standardDirectionOrder");
        data.remove("noticeOfProceedingsBundle");

        data.put("hiddenStandardDirectionOrder", standardDirectionOrder);

        data.put("state", GATEKEEPING);
    }
}
