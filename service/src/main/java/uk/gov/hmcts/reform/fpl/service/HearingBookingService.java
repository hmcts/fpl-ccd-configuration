package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;

@Service
public class HearingBookingService {

    public List<Element<HearingBooking>> expandHearingBookingCollection(CaseData caseData, DynamicList venues) {
        if (caseData.getHearingDetails() == null) {
            List<Element<HearingBooking>> populatedHearing = new ArrayList<>();

            populatedHearing.add(Element.<HearingBooking>builder()
                .value(HearingBooking.builder().venueList(venues).build())
                .build());

            return populatedHearing;
        } else {
            List<Element<HearingBooking>> hearings = new ArrayList<>();

            for (Element<HearingBooking> hearingBookingElement : caseData.getHearingDetails()) {
                HearingBooking booking = hearingBookingElement.getValue();
                DynamicList venueList = booking.getVenueList();
                // Update the old list with the current one
                if (venueList == null) {
                    venueList = venues;
                } else {
                    venueList = venueList.update(venues);
                }
                // Rebuild the element
                hearings.add(Element.<HearingBooking>builder().id(hearingBookingElement.getId())
                    .value(booking.toBuilder().venueList(venueList).build())
                    .build());
            }

            return hearings;
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
