package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
public class CMORemovalAction implements OrderRemovalAction {

    @Override
    public boolean isAccepted(RemovableOrder removableOrder) {
        return removableOrder instanceof CaseManagementOrder;
    }

    @Override
    public void remove(CaseData caseData, CaseDetailsMap data, UUID removedOrderId,
                       RemovableOrder removableOrder) {

        CaseManagementOrder caseManagementOrder = (CaseManagementOrder) removableOrder;

        List<Element<CaseManagementOrder>> sealedCMOs = caseData.getSealedCMOs();
        boolean removed = sealedCMOs.remove(element(removedOrderId, caseManagementOrder));
        if (!removed) {
            throw new CMONotFoundException(format("Failed to find order matching id %s", removedOrderId));
        }

        caseManagementOrder.setRemovalReason(caseData.getReasonToRemoveOrder());

        List<Element<CaseManagementOrder>> hiddenCMOs = caseData.getHiddenCMOs();
        hiddenCMOs.add(element(removedOrderId, caseManagementOrder));

        data.put("hiddenCaseManagementOrders", hiddenCMOs);
        data.putIfNotEmpty("sealedCMOs", sealedCMOs);
        data.put("hearingDetails", removeHearingLinkedToCMO(caseData.getHearingDetails(), removedOrderId));
    }

    @Override
    public void populateCaseFields(CaseData caseData,
                                   CaseDetailsMap data,
                                   UUID removableOrderId,
                                   RemovableOrder removableOrder) {
        CaseManagementOrder caseManagementOrder = (CaseManagementOrder) removableOrder;

        Optional<Element<HearingBooking>> hearingBooking = caseData.getHearingLinkedToCMO(removableOrderId);

        if (hearingBooking.isEmpty()) {
            throw new HearingNotFoundException(format("Could not find hearing matching id %s", removableOrderId));
        }

        data.put("orderToBeRemoved", caseManagementOrder.getOrder());
        data.put("orderTitleToBeRemoved", "Case management order");
        data.put("hearingToUnlink", hearingBooking.get().getValue().toLabel());
        data.put("showRemoveCMOFieldsFlag", YES.getValue());
    }

    private List<Element<HearingBooking>> removeHearingLinkedToCMO(List<Element<HearingBooking>> hearings,
                                                                  UUID removedOrderId) {
        if (isEmpty(hearings)) {
            return List.of();
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
