package uk.gov.hmcts.reform.fpl.exceptions;

import uk.gov.hmcts.reform.fpl.enums.DirectionType;

public class StandardDirectionNotFoundException extends RuntimeException {

    public StandardDirectionNotFoundException(DirectionType directionType) {
        super(String.format("Standard direction %s not found", directionType));
    }
}
