package uk.gov.hmcts.reform.fpl.model.order;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderSection {
    SECTION_1(false, true),
    SECTION_2(true, true),
    SECTION_3(true, true),
    SECTION_4(true, false);

    private final boolean validate;
    private final boolean prePopulate;

    public boolean shouldValidate() {
        return validate;
    }

    public boolean shouldPrePopulate() {
        return prePopulate;
    }

    public static OrderSection from(final String section) {
        final String parsed = section.toUpperCase().replace('-', '_');
        return OrderSection.valueOf(parsed);
    }
}
