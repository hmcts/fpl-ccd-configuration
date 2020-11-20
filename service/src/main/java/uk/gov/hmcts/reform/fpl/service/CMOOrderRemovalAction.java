package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
public class CMOOrderRemovalAction implements OrderRemovalAction {

    @Override
    public boolean isAccepted(RemovableOrder removableOrder) {
        return removableOrder instanceof CaseManagementOrder;
    }

    @Override
    public void action(CaseData caseData, Map<String, Object> data, UUID removedOrderId,
                       RemovableOrder removableOrder) {

        CaseManagementOrder caseManagementOrder = (CaseManagementOrder) removableOrder;

        List<Element<CaseManagementOrder>> sealedCMOs = caseData.getSealedCMOs();
        boolean removed = sealedCMOs.remove(element(removedOrderId, caseManagementOrder));
        if(!removed) {
            throw new IllegalArgumentException(format("Failed to find order matching id %s", removedOrderId));
        }

        caseManagementOrder.setRemovalReason(caseData.getReasonToRemoveOrder());

        List<Element<CaseManagementOrder>> hiddenCMOs = caseData.getHiddenCMOs();
        hiddenCMOs.add(element(removedOrderId, caseManagementOrder));

        data.put("hiddenCaseManagementOrders", hiddenCMOs);
        data.put("sealedCMOs", sealedCMOs);
        data.put("hearingDetails", removeHearingLinkedToCMO(caseData.getHearingDetails(), removedOrderId));
    }

    public List<Element<HearingBooking>> removeHearingLinkedToCMO(List<Element<HearingBooking>> hearings,
                                                                  UUID removedOrderId) {
        if(isEmpty(hearings)) {
            return null;
        }

        return hearings.stream()
            .map(element -> {
                HearingBooking hearingBooking = element.getValue();
                if (removedOrderId.equals(hearingBooking.getCaseManagementOrderId())) {
                    hearingBooking.setCaseManagementOrderId(null);
                }
                return element;
            }).collect(Collectors.toList());
    }

}
