package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum ApplicantType {
    LOCAL_AUTHORITY("Applicant"),
    RESPONDENT("Respondent"),
    OTHER("");

    private final String type;

    public static ApplicantType fromType(String value) {
        return Stream.of(ApplicantType.values())
            .filter(gender -> gender.getType().equalsIgnoreCase(value))
            .findFirst()
            .orElse(OTHER);
    }

}
