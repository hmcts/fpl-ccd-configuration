package uk.gov.hmcts.reform.fpl.enums;

public enum DocmosisTemplates {
    C6("FL-PLW-HNO-ENG-00092.docx", "Notice_of_proceedings_c6.pdf"),
    C6A("FL-PLW-HNO-ENG-00093.docx", "Notice_of_proceedings_c6a.pdf"),
    SDO("FL-PLW-STD-ENG-00099.doc", "standard_directions_order.pdf"),
    C21("FL-PLW-GOR-ENG-00218.docx", "C21_order.pdf");

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
