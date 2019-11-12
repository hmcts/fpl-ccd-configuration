package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicElementIndicator;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.UUID;

public class HearingDateHelper implements DynamicElementIndicator {
    private LocalDate date;
    private UUID id;
    private final DateFormatterService dateFormatterService;

    public HearingDateHelper(UUID id, LocalDate date, DateFormatterService dateFormatterService) {
        this.id = id;
        this.date = date;
        this.dateFormatterService = dateFormatterService;
    }

    @Override
    public DynamicListElement toDynamicElement() {
        final String dateString = convertDate(date);
        return DynamicListElement.builder().code(id.toString()).label(dateString).build();
    }

    private String convertDate(LocalDate date) {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }
}
