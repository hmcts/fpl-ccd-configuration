package uk.gov.hmcts.reform.fpl.enums;

public enum DocmosisTemplates {
    C6("C6.docx", "Notice_of_proceedings_(c6)"),
    C6A("C6a.docx", "Notice_of_proceedings_(c6a)");

    private final String template;
    private final String documentTitle;

    DocmosisTemplates(String template, String documentTitle) {
        this.template = template;
        this.documentTitle = documentTitle;
    }

    public String getTemplate() {
        return template;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }
}
