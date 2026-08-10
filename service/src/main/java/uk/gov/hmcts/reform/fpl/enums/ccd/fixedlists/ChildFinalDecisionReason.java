package uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChildFinalDecisionReason {
    FINAL_ORDER("Final order issued"),
    REFUSAL("Application refused"),
    WITHDRAWN("Application withdrawn"),
    NO_ORDER("No order made"),
    OTHER("Other"),
    HOUSEKEEPING("Housekeeping"),
    CONSOLIDATED("Case consolidated");

    private final String label;
}
