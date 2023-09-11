package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum ChildGender {

    @JsonProperty("Boy")
    BOY("Male", "Gwryw", "he", "himself", "Boy"),
    @JsonProperty("Girl")
    GIRL("Female", "Benyw", "she", "herself", "Girl"),
    @JsonProperty("Other")
    OTHER("They identify in another way", "Maent yn uniaethu mewn ffordd arall", "they", "themselves", "Other");

    private final String label;
    private final String welshLabel;
    private final String subjectPronoun;
    private final String reflexivePronoun;
    private final String roboticsLabel;

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

    public String getRoboticsLabel() {
        return roboticsLabel;
    }
}
