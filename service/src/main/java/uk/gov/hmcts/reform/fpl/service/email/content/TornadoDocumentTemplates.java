package uk.gov.hmcts.reform.fpl.service.email.content;

public enum TornadoDocumentTemplates {
    C6("C6.docx");

    private final String documentTemplate;

    TornadoDocumentTemplates(String documentTemplate) {
        this.documentTemplate = documentTemplate;
    }

    public String getDocumentTemplate() {
        return documentTemplate;
    }
}
