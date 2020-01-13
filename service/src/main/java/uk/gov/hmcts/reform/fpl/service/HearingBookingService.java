package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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

    public HearingBooking getMostUrgentHearingBooking(List<Element<HearingBooking>> hearingBookings) {
        if (hearingBookings == null) {
            throw new IllegalStateException("Hearing booking was not present");
        }

        return hearingBookings.stream()
            .map(Element::getValue)
            .min(comparing(HearingBooking::getStartDate))
            .orElseThrow(() -> new IllegalStateException("Expected to have at least one hearing booking"));
    }

    public HearingBooking getHearingBooking(final List<Element<HearingBooking>> hearingDetails,
                                            final DynamicList hearingDateList) {
        if (hearingDetails == null || hearingDateList == null || hearingDateList.getValue() == null) {
            return HearingBooking.builder().build();
        }

        return hearingDetails.stream()
            .filter(element -> element.getId().equals(hearingDateList.getValue().getCode()))
            .findFirst()
            .map(Element::getValue)
            .orElse(HearingBooking.builder().build());
    }

    public HearingBooking getHearingBookingByUUID(List<Element<HearingBooking>> hearingBookings, UUID elementId) {
        return hearingBookings.stream()
            .filter(hearingBookingElement -> hearingBookingElement.getId().equals(elementId))
            .map(Element::getValue)
            .findFirst()
            .orElse(null);
    }

    public List<Element<HearingBooking>> getChangedHearings(List<Element<HearingBooking>> before,
                                                            List<Element<HearingBooking>> after) {
        Map<UUID, LocalDateTime> times = before.stream()
            .filter(element -> element.getValue() != null && element.getValue().getStartDate() != null)
            .collect(toMap(Element::getId, element -> element.getValue().getStartDate()));

        return after.stream()
            .filter(hearing -> isNewHearing(times, hearing) || isExistingHearingWithDifferentStartTime(times, hearing))
            .collect(toList());
    }

    private boolean isNewHearing(Map<UUID, LocalDateTime> times, Element<HearingBooking> hearing) {
        return times.get(hearing.getId()) == null;
    }

    private boolean isExistingHearingWithDifferentStartTime(Map<UUID, LocalDateTime> times,
                                                            Element<HearingBooking> hearing) {
        return times.get(hearing.getId()) != null
            && !times.get(hearing.getId()).equals(hearing.getValue().getStartDate());
    }
}
