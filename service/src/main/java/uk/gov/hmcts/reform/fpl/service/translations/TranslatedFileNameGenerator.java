package uk.gov.hmcts.reform.fpl.service.translations;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Component
public class TranslatedFileNameGenerator {

    public String generate(CaseData caseData,
                           LanguageTranslationRequirement translationRequirements) {
        String filename = caseData.getUploadTranslationsEventData().getUploadTranslationsOriginalDoc().getFilename();

        return String.format("%s-%s.%s",
            FilenameUtils.removeExtension(filename),
            translationRequirements.getTargetLanguage().get().getLabel(),
            FilenameUtils.getExtension(filename)
        );
    }
}
