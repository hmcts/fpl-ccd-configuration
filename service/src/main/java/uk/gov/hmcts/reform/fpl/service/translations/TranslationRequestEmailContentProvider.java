package uk.gov.hmcts.reform.fpl.service.translations;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;

import java.util.Map;

@Component
public class TranslationRequestEmailContentProvider {

    private static final String TEMPLATE = "Please find attached the following documents for translation to "
        + "${targetLanguage}\n"
        + " Family Public Law digital service\n"
        + "\n"
        + " HM Courts & Tribunals Service\n"
        + "\n"
        + "Do not reply to this email. If you need to contact us, call 0330 808 4424 or email contactfpl@justice"
        + ".gov.uk";

    public String generate(LanguageTranslationRequirement language) {
        return new StringSubstitutor(Map.of(
            "targetLanguage", language.getTargetLanguage().get().getLabel())
        ).replace(TEMPLATE);
    }

}
