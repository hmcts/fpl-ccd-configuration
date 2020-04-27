package uk.gov.hmcts.reform.fpl.model.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IssuableOrder {
    boolean isSealed();

    @JsonIgnore
    default boolean isDraft() {
        return !isSealed();
    }
}
