package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class HearingBookingService {

    public List<Element<HearingBooking>> expandHearingBookingCollection(CaseData caseData) {
        return defaultIfNull(caseData.getHearingDetails(), newArrayList(element(HearingBooking.builder().build())));
    }

    public void filterHearingsInPast(List<Element<HearingBooking>> hearingDetails) {
        hearingDetails.removeIf(x -> x.getValue() != null && x.getValue().getStartDate() != null &&
            x.getValue().getStartDate().isBefore(LocalDateTime.now()));
    }

    public List<Element<HearingBooking>> getPastHearings(List<Element<HearingBooking>> allHearings,
                                                         List<Element<HearingBooking>> hearingsInFuture) {
        return allHearings.stream().filter(x -> !hearingsInFuture.contains(x)).collect(toList());
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

    public HearingBooking getHearingBooking(List<Element<HearingBooking>> hearingDetails, DynamicList hearingDateList) {
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

    public void addHearingsInPastFromBeforeDataState(List<Element<HearingBooking>> after,
                                                     List<Element<HearingBooking>> before) {
        after.addAll(before);
        after.sort(comparing(x -> x.getValue().getStartDate()));
    }
}
