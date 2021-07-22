package uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DocmosisLanguages {
    private boolean englishToWelsh;
    private boolean welshToEnglish;
}
