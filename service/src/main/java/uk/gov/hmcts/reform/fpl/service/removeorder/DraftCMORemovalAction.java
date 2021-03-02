package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.cmo.DraftOrderService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftCMORemovalAction implements OrderRemovalAction {

    public static final String DRAFT_UPLOADED_CMOS = "draftUploadedCMOs";
    private final DraftOrderService draftOrderService;
    private final UpdateCMOHearing updateCmoHearing;

    @Override
    public boolean isAccepted(RemovableOrder removableOrder) {
        return removableOrder instanceof HearingOrder && Optional.ofNullable(((HearingOrder) removableOrder).getType())
            .filter(HearingOrderType::isCmo)
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

        Optional<Element<HearingOrdersBundle>> optionalHearingOrderBundle
            = caseData.getHearingOrderBundleThatContainsOrder(removedOrderId);

        if (optionalHearingOrderBundle.isEmpty()) {
            throw new IllegalStateException(format("Failed to find hearing order bundle that contains order %s",
                removedOrderId));
        }

        Element<HearingOrdersBundle> selectedHearingOrderBundle = optionalHearingOrderBundle.get();
        Element<HearingOrder> cmoElement = element(removedOrderId, caseManagementOrder);

        selectedHearingOrderBundle.getValue().getOrders().remove(cmoElement);
        caseData.getDraftUploadedCMOs().remove(cmoElement);

        updateHearingOrderBundlesDrafts(data, caseData.getHearingOrdersBundlesDrafts(), selectedHearingOrderBundle);

        data.put("hearingDetails", updateCmoHearing.removeHearingLinkedToCMO(caseData, cmoElement));
        data.putIfNotEmpty(DRAFT_UPLOADED_CMOS, caseData.getDraftUploadedCMOs());
    }

    public void removeDraftCaseManagementOrder(CaseData caseData, CaseDetails data,
                                               Element<HearingOrder> cmoElement) {
        List<Element<HearingOrder>> draftUploadedCMOs = caseData.getDraftUploadedCMOs();

        if (!draftUploadedCMOs.remove(cmoElement)) {
            throw new CMONotFoundException("Failed to find draft case management order");
        }

        if (isEmpty(draftUploadedCMOs)) {
            data.getData().remove(DRAFT_UPLOADED_CMOS);
        } else {
            data.getData().put(DRAFT_UPLOADED_CMOS, draftUploadedCMOs);
        }

        data.getData().put("hearingOrdersBundlesDrafts", draftOrderService.migrateCmoDraftToOrdersBundles(caseData));
        data.getData().put("hearingDetails", updateCmoHearing.removeHearingLinkedToCMO(caseData, cmoElement));
    }

    private void updateHearingOrderBundlesDrafts(CaseDetailsMap data,
                                                 List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts,
                                                 Element<HearingOrdersBundle> selectedHearingOrderBundle) {
        List<Element<HearingOrdersBundle>> updatedHearingOrderBundle;

        if (selectedHearingOrderBundle.getValue().getOrders().isEmpty()) {
            updatedHearingOrderBundle = new ArrayList<>(hearingOrdersBundlesDrafts);
            updatedHearingOrderBundle.removeIf(bundle -> bundle.getId().equals(selectedHearingOrderBundle.getId()));
        } else {
            updatedHearingOrderBundle = hearingOrdersBundlesDrafts.stream()
                .map(hearingOrdersBundleElement -> {
                    if (selectedHearingOrderBundle.getId().equals(hearingOrdersBundleElement.getId())) {
                        return selectedHearingOrderBundle;
                    }

                    return hearingOrdersBundleElement;
                }).collect(Collectors.toList());
        }

        data.putIfNotEmpty("hearingOrdersBundlesDrafts", updatedHearingOrderBundle);
    }
}
