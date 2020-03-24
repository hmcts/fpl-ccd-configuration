package uk.gov.hmcts.reform.fpl.enums;

import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum ChildGender {

    BOY("Boy"), GIRL("Girl"), OTHER("They identify in another way");

    private final String label;

    public static ChildGender fromLabel(String label) {
        return Stream.of(ChildGender.values())
            .filter(gender -> gender.label.equalsIgnoreCase(label))
            .findFirst()
            .orElse(OTHER);
    }

    public String getLabel() {
        return label;
    }


}
