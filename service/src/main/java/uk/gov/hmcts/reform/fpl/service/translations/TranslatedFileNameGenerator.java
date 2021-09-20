package uk.gov.hmcts.reform.fpl.service.translations;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Component
public class TranslatedFileNameGenerator {

    public String generate(CaseData caseData) {
        String filename = caseData.getUploadTranslationsEventData().getUploadTranslationsOriginalDoc().getFilename();
        String base = FilenameUtils.removeExtension(filename);
        String extension = FilenameUtils.getExtension(filename);
        String amendedFileName = base + "-Welsh" + "." + extension;
        return amendedFileName;
    }
}
