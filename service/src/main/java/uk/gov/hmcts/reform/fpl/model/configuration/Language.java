package uk.gov.hmcts.reform.fpl.model.configuration;

public enum Language {
    ENGLISH("English"),
    WELSH("Welsh");

    private final String label;

    Language(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
