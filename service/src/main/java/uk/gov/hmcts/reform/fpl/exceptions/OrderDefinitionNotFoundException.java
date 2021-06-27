package uk.gov.hmcts.reform.fpl.exceptions;

import uk.gov.hmcts.reform.fpl.enums.DirectionType;

public class OrderDefinitionNotFoundException extends RuntimeException {

    public OrderDefinitionNotFoundException(DirectionType directionType) {
        super(String.format("Order definition %s not found", directionType));
    }
}
