package uk.gov.hmcts.reform.fpl.enums;

import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum ChildGender {

    BOY("Boy", "he", "himself"),
    GIRL("Girl", "she", "herself"),
    OTHER("They identify in another way", "they", "themselves");

    private final String label;
    private final String subjectPronoun;
    private final String reflexivePronoun;

    public static ChildGender fromLabel(String label) {
        return Stream.of(ChildGender.values())
            .filter(gender -> gender.label.equalsIgnoreCase(label))
            .findFirst()
            .orElse(OTHER);
    }

    public String getLabel() {
        return label;
    }


    public String getReflexivePronoun() {
        return reflexivePronoun;
    }

    public String getSubjectPronoun() {
        return subjectPronoun;
    }

}
