package uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocmosisTranslateLanguages {
    private boolean englishToWelsh;
    private boolean welshToEnglish;
}
