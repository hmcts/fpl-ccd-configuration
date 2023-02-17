package uk.gov.hmcts.reform.fpl.service.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendOrderReminderService {

    public List<HearingBooking> getPastHearingBookingsWithoutCMOs(CaseData caseData) {
        return caseData.getAllNonCancelledHearings().stream()
            .map(Element::getValue)
            .filter(booking -> booking.getEndDate().isBefore(now()) && !booking.hasCMOAssociation())
            .sorted(Comparator.comparing(HearingBooking::getStartDate))
            .collect(Collectors.toList());
    }
}
