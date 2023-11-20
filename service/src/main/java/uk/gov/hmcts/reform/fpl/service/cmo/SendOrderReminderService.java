package uk.gov.hmcts.reform.fpl.service.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendOrderReminderService {

    public List<HearingBooking> getPastHearingBookingsWithoutCMOs(CaseData caseData) {
        return caseData.getAllNonCancelledHearings().stream()
            .filter(booking -> booking.getValue().getEndDate().isBefore(now())
                && !booking.getValue().hasCMOAssociation()
                && !checkSealedCMOExistsForHearing(caseData, booking.getId()))
            .map(Element::getValue)
            .sorted(Comparator.comparing(HearingBooking::getStartDate))
            .collect(Collectors.toList());
    }

    public boolean checkSealedCMOExistsForHearing(CaseData caseData, UUID hearingId) {
        Optional<Element<HearingBooking>> booking = ElementUtils
            .findElement(hearingId, caseData.getAllNonCancelledHearings());

        // either it have a new style UUID hearing ID or an old style string hearing label (fallback)
        return caseData.getSealedCMOs().stream()
            .anyMatch(el -> hearingId.equals(el.getValue().getHearingId())
                || (booking.isPresent() && isNotEmpty(el.getValue().getHearing())
                    && el.getValue().getHearing().equals(booking.get().getValue().toLabel())));
    }
}
