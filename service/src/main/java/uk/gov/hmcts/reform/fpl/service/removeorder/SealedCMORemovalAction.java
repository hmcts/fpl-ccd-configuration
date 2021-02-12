package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.cmo.DraftOrderService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SealedCMORemovalAction implements OrderRemovalAction {

    private final DraftOrderService draftOrderService;
    private final UpdateCMOHearing updateCmoHearing;

    @Override
    public boolean isAccepted(RemovableOrder removableOrder) {
        return removableOrder instanceof HearingOrder
            && Optional.ofNullable(((HearingOrder) removableOrder).getStatus())
            .filter(APPROVED::equals)
            .isPresent();
    }

    @Override
    public void remove(CaseData caseData, CaseDetailsMap data, UUID removedOrderId,
                       RemovableOrder removableOrder) {

        HearingOrder caseManagementOrder = (HearingOrder) removableOrder;

        List<Element<HearingOrder>> sealedCMOs = caseData.getSealedCMOs();
        Element<HearingOrder> cmoElement = element(removedOrderId, caseManagementOrder);

        if (!sealedCMOs.remove(cmoElement)) {
            throw new CMONotFoundException(format("Failed to find order matching id %s", removedOrderId));
        }

        caseManagementOrder.setRemovalReason(caseData.getReasonToRemoveOrder());

        List<Element<HearingOrder>> hiddenCMOs = caseData.getHiddenCMOs();
        hiddenCMOs.add(cmoElement);

        data.put("hiddenCaseManagementOrders", hiddenCMOs);
        data.putIfNotEmpty("sealedCMOs", sealedCMOs);
        data.put("hearingDetails", updateCmoHearing.removeHearingLinkedToCMO(caseData, cmoElement));
    }

    public void removeDraftCaseManagementOrder(CaseData caseData, CaseDetails data,
                                               Element<HearingOrder> cmoElement) {
        List<Element<HearingOrder>> draftUploadedCMOs = caseData.getDraftUploadedCMOs();

        if (!draftUploadedCMOs.remove(cmoElement)) {
            throw new CMONotFoundException("Failed to find draft case management order");
        }

        if (isEmpty(draftUploadedCMOs)) {
            data.getData().remove("draftUploadedCMOs");
        } else {
            data.getData().put("draftUploadedCMOs", draftUploadedCMOs);
        }

        data.getData().put("hearingOrdersBundlesDrafts", draftOrderService.migrateCmoDraftToOrdersBundles(caseData));
        data.getData().put("hearingDetails", updateCmoHearing.removeHearingLinkedToCMO(caseData, cmoElement));
    }

    @Override
    public void populateCaseFields(CaseData caseData,
                                   CaseDetailsMap data,
                                   UUID removableOrderId,
                                   RemovableOrder removableOrder) {
        HearingOrder caseManagementOrder = (HearingOrder) removableOrder;

        HearingBooking hearing = updateCmoHearing.getHearingToUnlink(caseData, removableOrderId, caseManagementOrder);

        data.put("orderToBeRemoved", caseManagementOrder.getOrder());
        data.put("orderTitleToBeRemoved", "Sealed case management order");
        data.put("hearingToUnlink", hearing.toLabel());
        data.put("showRemoveCMOFieldsFlag", YES.getValue());
    }

}
