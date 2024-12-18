package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundles;
import uk.gov.hmcts.reform.fpl.service.cmo.DraftOrderService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftCMORemovalAction implements OrderRemovalAction {

    public static final String DRAFT_UPLOADED_CMOS = "draftUploadedCMOs";
    private final DraftOrderService draftOrderService;
    private final UpdateCMOHearing updateCmoHearing;
    private final UpdateHearingOrderBundlesDrafts updateHearingOrderBundlesDrafts;

    @Override
    public boolean isAccepted(RemovableOrder removableOrder) {
        return removableOrder instanceof HearingOrder && Optional.of((HearingOrder) removableOrder)
            .filter(order -> order.getType() != HearingOrderType.C21 && order.getStatus() != CMOStatus.RETURNED)
            .isPresent();
    }

    @Override
    public void populateCaseFields(CaseData caseData, CaseDetailsMap data, UUID removedOrderId,
                                   RemovableOrder removableOrder) {
        HearingOrder caseManagementOrder = (HearingOrder) removableOrder;
        HearingBooking hearing = updateCmoHearing.getHearingToUnlink(caseData, removedOrderId, caseManagementOrder);

        data.put("orderToBeRemoved", caseManagementOrder.getOrder());
        data.put("orderTitleToBeRemoved", "Draft case management order");
        data.put("hearingToUnlink", hearing.toLabel());
        data.put("showRemoveCMOFieldsFlag", YES.getValue());
        data.put("showReasonFieldFlag", NO.getValue());
    }

    @Override
    public void remove(CaseData caseData, CaseDetailsMap data, UUID removedOrderId, RemovableOrder removableOrder) {
        HearingOrder caseManagementOrder = (HearingOrder) removableOrder;

        if (isEmpty(caseData.getHearingOrdersBundlesDrafts())
            && isEmpty(caseData.getHearingOrdersBundlesDraftReview())) {

            Optional<Element<HearingOrder>> hearingOrderElement = caseData.getDraftUploadedCMOWithId(removedOrderId);

            if (hearingOrderElement.isEmpty()) {
                throw new IllegalStateException(format("Failed to find hearing order that contains order %s",
                    removedOrderId));
            }

            removeDraftCaseManagementOrder(caseData, data, hearingOrderElement.get());
        } else {
            Optional<Element<HearingOrdersBundle>> optionalHearingOrderBundle
                = caseData.getHearingOrderBundleThatContainsOrder(removedOrderId);

            if (optionalHearingOrderBundle.isEmpty()) {
                throw new IllegalStateException(format("Failed to find hearing order bundle that contains order %s",
                    removedOrderId));
            }

            Element<HearingOrdersBundle> selectedHearingOrderBundle = optionalHearingOrderBundle.get();
            Element<HearingOrder> cmoElement = element(removedOrderId, caseManagementOrder);
            HearingOrderType orderType = cmoElement.getValue().getType();

            selectedHearingOrderBundle.getValue().getOrders().remove(cmoElement);
            caseData.getDraftUploadedCMOs().remove(cmoElement);

            updateHearingOrderBundlesDrafts.update(
                selectedHearingOrderBundle,
                () -> orderType.equals(DRAFT_CMO) ? caseData.getHearingOrdersBundlesDraftReview()
                            : caseData.getHearingOrdersBundlesDrafts(),
                updatedHearingOrderBundle -> {
                    if (orderType.equals(DRAFT_CMO)) {
                        data.putIfNotEmpty("hearingOrdersBundlesDraftReview", updatedHearingOrderBundle);
                    } else {
                        data.putIfNotEmpty("hearingOrdersBundlesDrafts", updatedHearingOrderBundle);
                    }
                }
            );


            updateHearingDetailsWhenCMORemoved(caseData, data, cmoElement);
            data.putIfNotEmpty(DRAFT_UPLOADED_CMOS, caseData.getDraftUploadedCMOs());
        }
    }

    public void removeDraftCaseManagementOrder(CaseData caseData, CaseDetailsMap data,
                                               Element<HearingOrder> cmoElement) {
        List<Element<HearingOrder>> draftUploadedCMOs = caseData.getDraftUploadedCMOs();

        if (!draftUploadedCMOs.remove(cmoElement)) {
            throw new CMONotFoundException("Failed to find draft case management order");
        }

        if (isEmpty(draftUploadedCMOs)) {
            data.remove(DRAFT_UPLOADED_CMOS);
        } else {
            data.put(DRAFT_UPLOADED_CMOS, draftUploadedCMOs);
        }
        HearingOrdersBundles hearingOrdersBundles = draftOrderService.migrateCmoDraftToOrdersBundles(caseData);

        data.put("hearingOrdersBundlesDrafts", hearingOrdersBundles.getAgreedCmos());
        data.put("hearingOrdersBundlesDraftReview", hearingOrdersBundles.getDraftCmos());
        updateHearingDetailsWhenCMORemoved(caseData, data, cmoElement);
    }

    private void updateHearingDetailsWhenCMORemoved(CaseData caseData, CaseDetailsMap data,
                                                    Element<HearingOrder> cmoElement) {
        List<Element<HearingBooking>> updatedHearingBookings = updateCmoHearing.removeHearingLinkedToCMO(caseData,
            cmoElement);

        boolean removedFromCancelledHearings = updateCmoHearing.hearingLinkedToCMOIsCancelled(caseData,
            updatedHearingBookings.get(0).getValue());

        if (removedFromCancelledHearings) {
            data.put("cancelledHearingDetails", updatedHearingBookings);
        } else {
            data.put("hearingDetails", updatedHearingBookings);
        }
    }
}
