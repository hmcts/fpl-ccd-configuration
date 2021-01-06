package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.exceptions.removeorder.UnexpectedNumberOfCMOsRemovedException;
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
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CMORemovalAction implements OrderRemovalAction {

    private final DraftOrderService draftOrderService;

    @Override
    public boolean isAccepted(RemovableOrder removableOrder) {
        return removableOrder instanceof HearingOrder;
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
        data.put("hearingDetails", removeHearingLinkedToCMO(caseData, cmoElement));
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

        data.getData().put("draftHearingOrdersBundles", draftOrderService.migrateCmoDraftToOrdersBundles(caseData));
        data.getData().put("hearingDetails", removeHearingLinkedToCMO(caseData, cmoElement));
    }

    @Override
    public void populateCaseFields(CaseData caseData,
                                   CaseDetailsMap data,
                                   UUID removableOrderId,
                                   RemovableOrder removableOrder) {
        HearingOrder caseManagementOrder = (HearingOrder) removableOrder;

        HearingBooking hearing = getHearingToUnlink(caseData, removableOrderId, caseManagementOrder);

        data.put("orderToBeRemoved", caseManagementOrder.getOrder());
        data.put("orderTitleToBeRemoved", "Case management order");
        data.put("hearingToUnlink", hearing.toLabel());
        data.put("showRemoveCMOFieldsFlag", YES.getValue());
    }

    private List<Element<HearingBooking>> removeHearingLinkedToCMO(CaseData caseData,
                                                                   Element<HearingOrder> cmoElement) {

        HearingBooking hearingToUnlink = getHearingToUnlink(
            caseData,
            cmoElement.getId(),
            cmoElement.getValue()
        );

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
