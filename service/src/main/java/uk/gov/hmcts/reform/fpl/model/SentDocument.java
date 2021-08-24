package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SentDocument {
    String partyName;
    DocumentReference document;
    DocumentReference coversheet;
    String sentAt;
    String letterId;
    Language language;

    public Language getLanguage() {
        return defaultIfNull(language, ENGLISH);
    }
}
