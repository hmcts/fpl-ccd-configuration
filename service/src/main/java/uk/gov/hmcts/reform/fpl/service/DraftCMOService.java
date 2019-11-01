package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicElementParser;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DraftCMOService {

    public DynamicList makeHearingDateList(List<Element<HearingBooking>> hearingDetails) {
        List<HearingDate> hearingDates = hearingDetails
            .stream()
            .map(Element::getValue)
            .map(HearingBooking::getDate)
            .map(HearingDate::new)
            .collect(Collectors.toList());

        return DynamicList.toDynamicList(hearingDates, DynamicListElement.EMPTY);
    }

    class HearingDate implements DynamicElementParser {

        HearingDate(LocalDate date) {
            this.date = date;
        }

        private LocalDate date;

        @Override
        public DynamicListElement toDynamicElement() {
            return DynamicListElement.builder().code(date.toString()).label(date.toString()).build();
        }
    }
}
