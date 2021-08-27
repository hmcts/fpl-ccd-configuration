package uk.gov.hmcts.reform.fpl.service.translations.utils;

import uk.gov.hmcts.reform.fpl.model.configuration.Language;

public class TranslationContext {

    private TranslationContext() {
    }

    private static ThreadLocal<Language> applicationLanguage = new ThreadLocal<>();

    public static void setApplicationLanguage(Language applicationLanguage) {
        TranslationContext.applicationLanguage.set(applicationLanguage);
    }

    public static Language getApplicationLanguage() {
        return applicationLanguage.get();
    }
}
