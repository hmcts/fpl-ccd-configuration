package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RefusedHearingOrderRemovalAction implements OrderRemovalAction {

    @Override
    public boolean isAccepted(RemovableOrder removableOrder) {
        return removableOrder instanceof HearingOrder && Optional.of((HearingOrder) removableOrder)
            .filter(this::isRemovableOrder)
            .isPresent();
    }

    @Override
    public void populateCaseFields(CaseData caseData, CaseDetailsMap data, UUID removedOrderId,
                                   RemovableOrder removableOrder) {
        HearingOrder draftOrder = (HearingOrder) removableOrder;

        data.put("orderToBeRemoved", draftOrder.getOrder());
        data.put("orderTitleToBeRemoved", draftOrder.getTitle());
        data.put("showRemoveCMOFieldsFlag", EMPTY);
        data.put("showReasonFieldFlag", NO.getValue());
    }

    @Override
    public void remove(CaseData caseData, CaseDetailsMap data, UUID removedOrderId, RemovableOrder removableOrder) {
        HearingOrder draftOrder = (HearingOrder) removableOrder;

        Element<HearingOrder> refusedHearingOrderElement = caseData.getRefusedHearingOrders()
            .stream()
            .filter(element -> element.getId().equals(removedOrderId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to find refused hearing order"));

        List<Element<HearingOrder>> refusedHearingOrders = caseData.getRefusedHearingOrders();
        refusedHearingOrders.removeIf(element -> element.getId().equals(refusedHearingOrderElement.getId()));
        data.putIfNotEmpty("refusedHearingOrders", refusedHearingOrders);
    }

    private boolean isRemovableOrder(HearingOrder hearingOrder) {
        return RETURNED == hearingOrder.getStatus();
    }
}
