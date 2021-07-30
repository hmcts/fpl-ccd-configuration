package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;

public interface TranslatableItem extends ModifiableItem {

    boolean hasBeenTranslated();

    LocalDateTime translationUploadDateTime();

    DocumentReference getTranslatedDocument();

    default LanguageTranslationRequirement translationRequirements() {
        return ENGLISH_TO_WELSH;
    }

}
