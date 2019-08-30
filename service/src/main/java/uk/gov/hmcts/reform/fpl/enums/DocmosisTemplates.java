package uk.gov.hmcts.reform.fpl.enums;

public enum DocmosisTemplates {
    C6("C6.docx", "c6"),
    C6A("C6a.docx", "c6a");

    private final String templateName;
    private final String documentName;

    DocmosisTemplates(String templateName, String documentName) {
        this.templateName = templateName;
        this.documentName = documentName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getDocumentName() {
        return documentName;
    }
}
