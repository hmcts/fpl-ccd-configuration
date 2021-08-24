package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;

@Value
@Builder(toBuilder = true)
public class DocumentReferenceWithLanguage {

    DocumentReference documentReference;
    Language language;

    public Language getLanguage() {
        return defaultIfNull(language,ENGLISH);
    }
}
