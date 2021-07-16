package uk.gov.hmcts.reform.fpl.model.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface TranslatableItem {
    String asLabel();

    @JsonIgnore
    default boolean needTranslation() {
        return true;
    }

    boolean hasBeenTranslated();
}
