package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicElementParser;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DraftCMOService {

    private final DateFormatterService dateFormatterService;

    @Autowired
    public DraftCMOService(DateFormatterService dateFormatterService) {
        this.dateFormatterService = dateFormatterService;
    }

    public DynamicList buildDynamicListFromHearingDetails(List<Element<HearingBooking>> hearingDetails) {
        List<HearingDateDynamicElement> hearingDates = hearingDetails
            .stream()
            .map(Element::getValue)
            .map(HearingBooking::getDate)
            .map(HearingDateDynamicElement::new)
            .collect(Collectors.toList());

        return DynamicList.toDynamicList(hearingDates, DynamicListElement.EMPTY);
    }

    public String convertDate(LocalDate date) {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }

    class HearingDateDynamicElement implements DynamicElementParser {

        HearingDateDynamicElement(LocalDate date) {
            this.date = date;
        }

        private LocalDate date;

        @Override
        public DynamicListElement toDynamicElement() {
            final String dateString = convertDate(date);
            return DynamicListElement.builder().code(dateString).label(dateString).build();
        }
    }
}
