package uk.gov.hmcts.reform.fpl.model.order;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderSection {
    SECTION_1,
    SECTION_2,
    SECTION_3,
    SECTION_4;

    public static OrderSection from(final String section) {
        final String parsed = section.toUpperCase().replace('-', '_');
        return OrderSection.valueOf(parsed);
    }

}
