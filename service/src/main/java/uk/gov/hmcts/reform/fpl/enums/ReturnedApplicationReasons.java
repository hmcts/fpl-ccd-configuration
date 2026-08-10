package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ReturnedReason", generate = true)
@Getter
@RequiredArgsConstructor
public enum ReturnedApplicationReasons {
    @CCD(label = "Application Incorrect")
    INCORRECT("Application Incorrect"),
    @CCD(label = "Application Incomplete")
    INCOMPLETE("Application Incomplete"),
    @CCD(label = "Clarification Needed")
    CLARIFICATION_NEEDED("Clarification Needed");

    private final String label;
}
