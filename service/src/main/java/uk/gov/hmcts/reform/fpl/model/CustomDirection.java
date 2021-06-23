package uk.gov.hmcts.reform.fpl.model;

import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;

import static uk.gov.hmcts.reform.fpl.enums.DirectionType.CUSTOM;

@Jacksonized
@SuperBuilder(toBuilder = true)
public class CustomDirection extends StandardDirection {
    public DirectionType getType() {
        return CUSTOM;
    }
}
