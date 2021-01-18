package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GeneratedEPOKey {
    DATE_AND_TIME_OF_ISSUE("dateAndTimeOfIssue"),
    EPO_REMOVAL_ADDRESS("epoRemovalAddress"),
    EPO_CHILDREN("epoChildren"),
    EPO_END_DATE("epoEndDate"),
    EPO_PHRASE("epoPhrase"),
    EPO_TYPE("epoType"),
    EPO_WHO_IS_EXCLUDED("epoWhoIsExcluded"),
    EPO_EXCLUSION_REQUIREMENT_TYPE("epoExclusionRequirementType"),
    EPO_EXCLUSION_START_DATE("epoExclusionStartDate");

    private final String key;
}
