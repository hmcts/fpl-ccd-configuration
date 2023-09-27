package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JudgeCaseRole {

    ALLOCATED_JUDGE("allocated-judge"),
    HEARING_JUDGE("hearing-judge");

    private final String roleName;

}
