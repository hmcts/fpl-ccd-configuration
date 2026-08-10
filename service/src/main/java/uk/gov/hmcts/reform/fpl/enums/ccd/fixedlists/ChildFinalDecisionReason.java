package uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ChildFinalDecision", generate = true)
@Getter
@RequiredArgsConstructor
public enum ChildFinalDecisionReason {
    @CCD(label = "Final order issued")
    FINAL_ORDER("Final order issued"),
    @CCD(label = "Application refused")
    REFUSAL("Application refused"),
    @CCD(label = "Application withdrawn")
    WITHDRAWN("Application withdrawn"),
    @CCD(label = "No order made")
    NO_ORDER("No order made"),
    OTHER("Other"),
    @CCD(label = "Housekeeping")
    HOUSEKEEPING("Housekeeping"),
    @CCD(label = "Case consolidated")
    CONSOLIDATED("Case consolidated");

    private final String label;
}
