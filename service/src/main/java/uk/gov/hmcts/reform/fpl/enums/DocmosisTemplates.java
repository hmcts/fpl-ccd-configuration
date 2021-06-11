package uk.gov.hmcts.reform.fpl.enums;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public enum DocmosisTemplates {
    C110A("FL-PLW-APP-ENG-00095.doc", "c110a_application_%s"),
    C6("FL-PLW-HNO-ENG-00092.doc", "Notice_of_proceedings_c6"),
    C6A("FL-PLW-HNO-ENG-00093.doc", "Notice_of_proceedings_c6a"),
    NOTICE_OF_HEARING("FL-PLW-HNO-ENG-00525.doc", "Notice_of_hearing"),
    SDO("FL-PLW-STD-ENG-00099.doc", "standard-directions-order"),
    //fallback title 'order.pdf' - file name is generated in GeneratedOrderService based on type of order
    ORDER("FL-PLW-GOR-ENG-00218.doc", "order"),
    SGO("FL-C43aSpecialGuardianshipOrder.doc", "special-guardianship-order"),
    EPO("FL-PLW-GOR-ENG-00228.doc", "emergency_protection_order"),
    COVER_DOCS("FL-PLW-LET-ENG-00232.doc", "cover_documents");

    //TODO: 11/06/2021 Update document with correct name
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
        return documentTitle + ".pdf";
    }

    public String getDocumentTitle(LocalDate date) {
        return String.format("%s_%s.%s", documentTitle, date.format(DateTimeFormatter.ofPattern("ddMMMM")), "pdf");
    }
}
