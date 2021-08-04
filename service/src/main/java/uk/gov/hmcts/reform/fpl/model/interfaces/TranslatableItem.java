package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;

public interface TranslatableItem extends ModifiableItem {

    boolean hasBeenTranslated();

    LocalDateTime translationUploadDateTime();

    DocumentReference getTranslatedDocument();

    LanguageTranslationRequirement getTranslationRequirements();

    YesNo getNeedTranslation();

}
