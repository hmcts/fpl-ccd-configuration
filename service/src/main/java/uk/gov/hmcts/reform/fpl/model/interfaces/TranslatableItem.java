package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TranslatableItem extends ModifiableItem {

    boolean hasBeenTranslated();

    LocalDateTime translationUploadDateTime();

    DocumentReference getTranslatedDocument();

    LanguageTranslationRequirement getTranslationRequirements();

    default YesNo getNeedTranslation() {
        return YesNo.from(Optional.ofNullable(getTranslationRequirements())
            .map(LanguageTranslationRequirement::isNeedAction)
            .orElse(false));
    }

}
