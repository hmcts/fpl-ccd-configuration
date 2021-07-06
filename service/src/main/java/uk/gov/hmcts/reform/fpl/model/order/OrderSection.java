package uk.gov.hmcts.reform.fpl.model.order;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderSection {
    ORDER_SELECTION,
    HEARING_DETAILS,
    ISSUING_DETAILS,
    CHILDREN_DETAILS,
    ORDER_DETAILS,
    REVIEW,
    OTHER_DETAILS;

    public static OrderSection from(final String section) {
        final String parsed = section.toUpperCase().replace('-', '_');
        return OrderSection.valueOf(parsed);
    }

}
