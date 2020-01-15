package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingBookingKeys.HEARING_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.HearingBookingKeys.PAST_HEARING_DETAILS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class HearingBookingService {
    private static final List<Element<HearingBooking>> EMPTY_HEARING_BOOKING =
        newArrayList(element(HearingBooking.builder().build()));

    public List<Element<HearingBooking>> expandHearingBookingCollection(CaseData caseData) {
        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();

        return hearingDetails != null ? hearingDetails : EMPTY_HEARING_BOOKING;
    }

    /**
     * Creates a map with past and future hearings.
     *
     * @param hearingDetails a list of hearing bookings.
     * @return map of past and future hearings.
     */
    public Map<String, List<Element<HearingBooking>>> splitPastAndFutureHearings(
        List<Element<HearingBooking>> hearingDetails) {
        List<Element<HearingBooking>> pastHearingsDetails = getPastHearings(hearingDetails);
        hearingDetails.removeIf(pastHearingsDetails::contains);

        return Map.of(
            HEARING_DETAILS.getKey(), isNotEmpty(hearingDetails) ? hearingDetails : EMPTY_HEARING_BOOKING,
            PAST_HEARING_DETAILS.getKey(), pastHearingsDetails);
    }

    private List<Element<HearingBooking>> getPastHearings(List<Element<HearingBooking>> hearingDetails) {
        return hearingDetails.stream().filter(this::isBeforeToday).collect(toList());
    }

    private boolean isBeforeToday(Element<HearingBooking> element) {
        return element.getValue() != null && element.getValue().getStartDate() != null
            && element.getValue().getStartDate().isBefore(LocalDateTime.now());
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
    public List<Element<HearingBooking>> rebuildHearingDetailsObject(List<Element<HearingBooking>> firstList,
                                                                     List<Element<HearingBooking>> secondList) {
        List<Element<HearingBooking>> combinedHearingDetails = newArrayList();
        combinedHearingDetails.addAll(firstList);
        combinedHearingDetails.addAll(secondList);

        combinedHearingDetails.sort(comparing(element -> element.getValue().getStartDate()));

        return combinedHearingDetails;
    }
}
