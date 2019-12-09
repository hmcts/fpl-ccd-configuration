package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public HearingBooking getMostUrgentHearingBooking(List<Element<HearingBooking>> hearingBookings) {
        if (hearingBookings == null) {
            throw new IllegalStateException("Hearing booking was not present");
        }

        return hearingBookings.stream()
            .map(Element::getValue)
            .min(comparing(HearingBooking::getStartDate))
            .orElseThrow(() -> new IllegalStateException("Expected to have at least one hearing booking"));
    }

    public HearingBooking getHearingBookingByUUID(List<Element<HearingBooking>> hearingBookings, UUID elementId) {
        return hearingBookings.stream()
            .filter(hearingBookingElement -> hearingBookingElement.getId().equals(elementId))
            .map(Element::getValue)
            .findFirst()
            .orElse(HearingBooking.builder().build());
    }

    public HearingDateDynamicElement getHearingDynamicElement(DynamicList list) {
        return HearingDateDynamicElement.builder()
            .date(list.getValue().getLabel())
            .id(list.getValue().getCode())
            .build();
    }
}
