package uk.gov.hmcts.reform.fpl.enums;

import java.util.stream.Stream;

public enum DirectionAssignee {
    ALL_PARTIES("allParties"),
    LOCAL_AUTHORITY("localAuthorityDirections"),
    PARENTS_AND_RESPONDENTS("parentsAndRespondentsDirections"),
    CAFCASS("cafcassDirections"),
    OTHERS("otherPartiesDirections"),
    COURT("courtDirections");

    private final String value;

    DirectionAssignee(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DirectionAssignee fromString(String value) {
        return Stream.of(DirectionAssignee.values())
            .filter(x -> x.getValue().equals(value))
            .findFirst()
            .orElse(null);
    }
}
