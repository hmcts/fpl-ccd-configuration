package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.exceptions.removeorder.UnexpectedNumberOfCMOsRemovedException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftCMORemovalAction implements OrderRemovalAction {

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
        HearingBooking hearing = getHearingToUnlink(caseData, removedOrderId, caseManagementOrder);

        data.put("orderToBeRemoved", caseManagementOrder.getOrder());
        data.put("orderTitleToBeRemoved", "Draft case management order");
        data.put("hearingToUnlink", hearing.toLabel());
        data.put("showRemoveCMOFieldsFlag", YES.getValue());
    }

    @Override
    public void remove(CaseData caseData, CaseDetailsMap data, UUID removedOrderId, RemovableOrder removableOrder) {
        HearingOrder caseManagementOrder = (HearingOrder) removableOrder;

        List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = caseData.getHearingOrdersBundlesDrafts();
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

        updateHearingOrderBundlesDrafts(data, hearingOrdersBundlesDrafts, selectedHearingOrderBundle);

        data.put("hearingDetails", removeHearingLinkedToCMO(caseData, cmoElement));
        data.putIfNotEmpty("draftUploadedCMOs", caseData.getDraftUploadedCMOs());
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

    private List<Element<HearingBooking>> removeHearingLinkedToCMO(CaseData caseData,
                                                                   Element<HearingOrder> cmoElement) {
        HearingBooking hearingToUnlink = getHearingToUnlink(
            caseData,
            cmoElement.getId(),
            cmoElement.getValue());

        // this will still be the same reference as the one in the case data list so just update it
        hearingToUnlink.setCaseManagementOrderId(null);

        return caseData.getHearingDetails();
    }

    private HearingBooking getHearingToUnlink(CaseData caseData, UUID cmoId, HearingOrder cmo) {
        Optional<Element<HearingBooking>> hearingBooking = caseData.getHearingLinkedToCMO(cmoId);

        if (hearingBooking.isEmpty()) {
            List<Element<HearingBooking>> matchingLabel = caseData.getHearingDetails()
                .stream()
                .filter(hearing -> hearing.getValue().toLabel().equals(cmo.getHearing()))
                .collect(Collectors.toList());

            if (matchingLabel.size() != 1) {
                throw new UnexpectedNumberOfCMOsRemovedException(
                    cmoId,
                    format("CMO %s could not be linked to hearing by CMO id and there wasn't a unique link "
                        + "(%s links found) to a hearing with the same label", cmoId, matchingLabel.size())
                );
            }

            return matchingLabel.get(0).getValue();
        }
        return hearingBooking.get().getValue();
    }
}
