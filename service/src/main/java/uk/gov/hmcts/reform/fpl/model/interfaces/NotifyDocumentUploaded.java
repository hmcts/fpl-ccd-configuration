package uk.gov.hmcts.reform.fpl.model.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

public interface NotifyDocumentUploaded {
    DocumentReference getDocument();

    LanguageTranslationRequirement getTranslationRequirements();

    @JsonIgnore
    default String getNameForNotification() {
        return getDocument().getFilename();
    }
}
