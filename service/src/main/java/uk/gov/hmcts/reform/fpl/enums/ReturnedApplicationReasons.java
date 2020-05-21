package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReturnedApplicationReasons {
    INCORRECT("Application Incorrect"),
    INCOMPLETE("Application Incomplete"),
    CLARIFICATION_NEEDED("Clarification Needed");

    private final String label;
}
