package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Component
public class SDORemovalAction implements OrderRemovalAction {

    @Override
    public boolean isAccepted(RemovableOrder removableOrder) {
        return removableOrder instanceof StandardDirectionOrder;
    }

    @Override
    public void populateCaseFields(CaseData caseData, CaseDetailsMap data, UUID removedOrderId,
                                   RemovableOrder removableOrder) {
        StandardDirectionOrder standardDirectionOrder = (StandardDirectionOrder) removableOrder;

        data.put("orderToBeRemoved", standardDirectionOrder.getOrderDoc());
        data.put("orderTitleToBeRemoved", "Gatekeeping order");
        data.put("showRemoveSDOWarningFlag", YES.getValue());
    }

    @Override
    public void remove(CaseData caseData, CaseDetailsMap data, UUID removedOrderId, RemovableOrder removableOrder) {
        StandardDirectionOrder standardDirectionOrder = (StandardDirectionOrder) removableOrder;

        standardDirectionOrder.setRemovalReason(caseData.getReasonToRemoveOrder());
        standardDirectionOrder = standardDirectionOrder.toBuilder().judgeAndLegalAdvisor(null).build();

        data.remove("standardDirectionOrder");
        data.remove("noticeOfProceedingsBundle");

        data.put("hiddenStandardDirectionOrder", standardDirectionOrder);
        data.put("state", GATEKEEPING);
    }
}
