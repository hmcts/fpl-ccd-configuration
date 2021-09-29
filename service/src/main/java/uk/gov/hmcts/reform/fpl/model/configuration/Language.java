package uk.gov.hmcts.reform.fpl.model.configuration;

import java.util.Locale;

public enum Language {
    ENGLISH("English", Locale.UK),
    WELSH("Welsh", Locale.forLanguageTag("cy"));

    private final String label;
    private final Locale locale;

    Language(String label, Locale locale) {
        this.label = label;
        this.locale = locale;
    }

    public String getLabel() {
        return label;
    }

    public Locale getLocale() {
        return locale;
    }
}
