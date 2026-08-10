package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum C43OrderType {
    @CCD(label = "Child arrangements order")
    CHILD_ARRANGEMENT_ORDER("Child arrangements"),
    @CCD(label = "Specific issue order")
    SPECIFIC_ISSUE_ORDER("Specific issue"),
    @CCD(label = "Prohibited steps order")
    PROHIBITED_STEPS_ORDER("Prohibited steps");

    private final String label;
}
