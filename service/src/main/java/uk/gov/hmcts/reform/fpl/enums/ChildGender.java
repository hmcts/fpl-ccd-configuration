package uk.gov.hmcts.reform.fpl.enums;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum ChildGender {

    BOY("Boy", "Bachgen", "he", "himself"),
    GIRL("Girl", "Merch", "she", "herself"),
    OTHER("They identify in another way", "Maent yn uniaethu mewn ffordd arall", "they", "themselves");

    private final String label;
    private final String welshLabel;
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

    public String getLabel(Language language) {
        return language == Language.WELSH ? welshLabel : label;
    }

    public String getReflexivePronoun() {
        return reflexivePronoun;
    }

    public String getSubjectPronoun() {
        return subjectPronoun;
    }

}
