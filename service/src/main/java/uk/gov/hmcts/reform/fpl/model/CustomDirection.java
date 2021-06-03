package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;

import static uk.gov.hmcts.reform.fpl.enums.DirectionType.CUSTOM;

@SuperBuilder
@Jacksonized
public class CustomDirection extends StandardDirection {

    @JsonIgnore
    public DirectionType getType() {
        return CUSTOM;
    }
}
