package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

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
            return HearingBooking.builder().build();
        }

        return caseData.getHearingDetails().stream()
            .map(Element::getValue)
            .sorted((booking1, booking2) -> booking1.getDate().compareTo(booking2.getDate()))
            .findFirst()
            .orElseThrow();
    }
}
