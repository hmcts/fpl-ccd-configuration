package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.LocalDateTime.now;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class HearingBookingService {
    public static final String HEARING_DETAILS_KEY = "hearingDetails";

    public List<Element<HearingBooking>> expandHearingBookingCollection(CaseData caseData) {
        return ofNullable(caseData.getHearingDetails())
            .orElse(newArrayList(element(HearingBooking.builder().build())));
    }

    public List<Element<HearingBooking>> getPastHearings(List<Element<HearingBooking>> hearingDetails) {
        return hearingDetails.stream().filter(this::isPastHearing).collect(toList());
    }

    private boolean isPastHearing(Element<HearingBooking> element) {
        return ofNullable(element.getValue())
            .map(HearingBooking::getStartDate)
            .filter(hearingDate -> hearingDate.isBefore(now()))
            .isPresent();
    }

    public HearingBooking getMostUrgentHearingBooking(List<Element<HearingBooking>> hearingDetails) {
        if (hearingDetails == null) {
            throw new IllegalStateException("Hearing booking was not present");
        }

        return hearingDetails.stream()
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

    public HearingBooking getHearingBookingByUUID(List<Element<HearingBooking>> hearingDetails, UUID elementId) {
        return hearingDetails.stream()
            .filter(hearingBookingElement -> hearingBookingElement.getId().equals(elementId))
            .map(Element::getValue)
            .findFirst()
            .orElse(null);
    }

    /**
     * Combines two lists of hearings into one, ordered by start date.
     * Implemented due to work around with hearing start date validation.
     *
     * @param firstList  the first list of hearing bookings to combine.
     * @param secondList the second list of hearing bookings to combine.
     * @return an ordered list of hearing bookings.
     */
    public List<Element<HearingBooking>> combineHearingDetails(List<Element<HearingBooking>> firstList,
                                                               List<Element<HearingBooking>> secondList) {
        List<Element<HearingBooking>> combinedHearingDetails = newArrayList();
        combinedHearingDetails.addAll(firstList);
        combinedHearingDetails.addAll(secondList);

        combinedHearingDetails.sort(comparing(element -> element.getValue().getStartDate()));

        return combinedHearingDetails;
    }
}
