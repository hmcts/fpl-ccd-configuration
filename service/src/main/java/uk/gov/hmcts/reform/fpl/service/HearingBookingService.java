package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;

@Service
public class HearingBookingService {

    public List<Element<HearingBooking>> expandHearingBookingCollection(CaseData caseData) {
        if (caseData.getHearingDetails() == null) {
            List<Element<HearingBooking>> populatedHearing = new ArrayList<>();

            populatedHearing.add(Element.<HearingBooking>builder()
                .value(HearingBooking.builder().build())
                .build());
            return populatedHearing;
        } else {
            return caseData.getHearingDetails();
        }
    }

    public HearingBooking getMostUrgentHearingBooking(CaseData caseData) {
        if (caseData.getHearingDetails() == null) {
            throw new IllegalStateException("Hearing booking was not present");
        }

        return caseData.getHearingDetails().stream()
            .map(Element::getValue)
            .min(comparing(HearingBooking::getDate))
            .orElseThrow(() -> new IllegalStateException("Expected to have at least one hearing booking"));
    }
}
