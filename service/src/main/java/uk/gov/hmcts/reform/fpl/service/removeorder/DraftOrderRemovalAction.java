package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrderRemovalAction implements OrderRemovalAction {

    private final UpdateHearingOrderBundlesDrafts updateHearingOrderBundlesDrafts;

    @Override
    public boolean isAccepted(RemovableOrder removableOrder) {
        return removableOrder instanceof HearingOrder && Optional.of((HearingOrder) removableOrder)
            .filter(this::isRemovableOrder)
            .isPresent();
    }

    @Override
    public void remove(CaseData caseData, CaseDetailsMap data, UUID removedOrderId, RemovableOrder removableOrder) {
        HearingOrder draftOrder = (HearingOrder) removableOrder;

        Optional<Element<HearingOrdersBundle>> optionalHearingOrderBundle
            = caseData.getHearingOrderBundleThatContainsOrder(removedOrderId);

        Element<HearingOrdersBundle> selectedHearingOrderBundle =
            optionalHearingOrderBundle.orElseThrow(
                () -> new IllegalStateException(
                    format("Failed to find hearing order bundle that contains order %s", removedOrderId)));

        Element<HearingOrder> draftOrderElement = element(removedOrderId, draftOrder);

        selectedHearingOrderBundle.getValue().getOrders().remove(draftOrderElement);
        updateHearingOrderBundlesDrafts.update(
            data, caseData.getHearingOrdersBundlesDrafts(), selectedHearingOrderBundle);
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

    private boolean isRemovableOrder(HearingOrder hearingOrder) {
        return C21 == hearingOrder.getType() && SEND_TO_JUDGE == hearingOrder.getStatus();
    }

}
