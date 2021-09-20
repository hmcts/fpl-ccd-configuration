package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.exceptions.removaltool.UnexpectedNumberOfCMOsRemovedException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Component
public class UpdateCMOHearing {

    public HearingBooking getHearingToUnlink(CaseData caseData, UUID cmoId, HearingOrder cmo) {
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

    public List<Element<HearingBooking>> removeHearingLinkedToCMO(CaseData caseData,
                                                                  Element<HearingOrder> cmoElement) {
        HearingBooking hearingToUnlink = getHearingToUnlink(
            caseData,
            cmoElement.getId(),
            cmoElement.getValue());

        // this will still be the same reference as the one in the case data list so just update it
        hearingToUnlink.setCaseManagementOrderId(null);

        return caseData.getHearingDetails();
    }

}
