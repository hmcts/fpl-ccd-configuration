package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum C43OrderType {
    CHILD_ARRANGEMENT_ORDER("Child arrangements"),
    SPECIFIC_ISSUE_ORDER("Specific issue"),
    PROHIBITED_STEPS_ORDER("Prohibited steps");

    private final String label;
}
