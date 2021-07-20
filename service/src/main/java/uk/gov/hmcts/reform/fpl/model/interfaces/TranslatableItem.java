package uk.gov.hmcts.reform.fpl.model.interfaces;

import java.time.LocalDateTime;

public interface TranslatableItem {
    String asLabel();

    boolean hasBeenTranslated();

    LocalDateTime translationUploadDateTime();
}
