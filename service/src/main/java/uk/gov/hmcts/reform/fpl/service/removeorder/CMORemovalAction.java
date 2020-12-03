package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.exceptions.removeorder.UnexpectedNumberOfCMOsRemovedException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
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
        Element<CaseManagementOrder> cmoElement = element(removedOrderId, caseManagementOrder);

        if (!sealedCMOs.remove(cmoElement)) {
            throw new CMONotFoundException(format("Failed to find order matching id %s", removedOrderId));
        }

        caseManagementOrder.setRemovalReason(caseData.getReasonToRemoveOrder());

        List<Element<CaseManagementOrder>> hiddenCMOs = caseData.getHiddenCMOs();
        hiddenCMOs.add(cmoElement);

        data.put("hiddenCaseManagementOrders", hiddenCMOs);
        data.putIfNotEmpty("sealedCMOs", sealedCMOs);
        data.put("hearingDetails", removeHearingLinkedToCMO(caseData.getHearingDetails(), cmoElement));
    }

    @Override
    public void populateCaseFields(CaseData caseData,
                                   CaseDetailsMap data,
                                   UUID removableOrderId,
                                   RemovableOrder removableOrder) {
        CaseManagementOrder caseManagementOrder = (CaseManagementOrder) removableOrder;

        Optional<Element<HearingBooking>> hearingBooking = caseData.getHearingLinkedToCMO(removableOrderId);

        if (hearingBooking.isEmpty()) {
            IncrementalInteger counter = new IncrementalInteger();
            Element<HearingBooking> foundHearing = null;

            for (Element<HearingBooking> hearing : caseData.getHearingDetails()) {
                if (hearing.getValue().toLabel().equals(caseManagementOrder.getHearing())) {
                    foundHearing = hearing;
                    if (counter.incrementAndGet() > 1) {
                        // stop the loop early, already found too many
                        break;
                    }
                }
            }

            // won't be null but stops nullable complaints later on
            if (counter.getValue() != 1 || foundHearing == null) {
                throw new UnexpectedNumberOfCMOsRemovedException(
                    removableOrderId,
                    format("CMO %s could not be linked to hearing by CMO id and there wasn't a unique link "
                        + "(%s links found) to a hearing with the same label", removableOrderId, counter.getValue())
                );
            }

            hearingBooking = Optional.of(foundHearing);
        }

        data.put("orderToBeRemoved", caseManagementOrder.getOrder());
        data.put("orderTitleToBeRemoved", "Case management order");
        data.put("hearingToUnlink", hearingBooking.get().getValue().toLabel());
        data.put("showRemoveCMOFieldsFlag", YES.getValue());
    }

    private List<Element<HearingBooking>> removeHearingLinkedToCMO(List<Element<HearingBooking>> hearings,
                                                                   Element<CaseManagementOrder> cmoElement) {
        // QUESTION: 03/12/2020 Do we need this empty check?
        if (isEmpty(hearings)) {
            return List.of();
        }

        IncrementalInteger counter = new IncrementalInteger();
        UUID id = cmoElement.getId();
        CaseManagementOrder cmo = cmoElement.getValue();

        List<Element<HearingBooking>> updatedHearings = updateOnCondition(
            hearings,
            hearing -> id.equals(hearing.getValue().getCaseManagementOrderId()),
            counter
        );

        switch (counter.getValue()) {
            case 0:
                // use label instead
                counter.reset();
                updatedHearings = updateOnCondition(
                    hearings,
                    hearing -> cmo.getHearing().equals(hearing.getValue().toLabel()),
                    counter
                );

                // QUESTION: 03/12/2020 are these additional checks required seeing as it should have been guarded in
                //  the mid event?
                if (counter.getValue() == 1) {
                    return updatedHearings;
                } else {
                    throw new UnexpectedNumberOfCMOsRemovedException(
                        id,
                        format("CMO %s could not be linked to hearing by CMO id and there wasn't a unique link "
                            + "(%s links found) to a hearing with the same label", id, counter.getValue())
                    );
                }
            case 1:
                return updatedHearings;
            default:
                // more than one hearing was linked to the cmo, situation should not occur but covers the default switch
                throw new UnexpectedNumberOfCMOsRemovedException(
                    id,
                    format("CMO %s was linked to multiple hearings by id", id)
                );
        }
    }

    private List<Element<HearingBooking>> updateOnCondition(List<Element<HearingBooking>> hearings,
                                                            Predicate<Element<HearingBooking>> linkTest,
                                                            IncrementalInteger counter) {

        return hearings.stream()
            .map(hearing -> {
                if (linkTest.test(hearing)) {
                    counter.increment();
                    hearing.getValue().setCaseManagementOrderId(null);
                }
                return hearing;
            })
            .collect(Collectors.toList());
    }
}
