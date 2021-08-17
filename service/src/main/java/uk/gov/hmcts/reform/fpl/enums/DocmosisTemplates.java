package uk.gov.hmcts.reform.fpl.enums;

import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public enum DocmosisTemplates {
    C110A("FL-PLW-APP-ENG-00095.doc", "FL-PLW-APP-WEL-00753.doc", "c110a_application_%s"),
    C6("FL-PLW-HNO-ENG-00092.doc", null, "Notice_of_proceedings_c6"),
    C6A("FL-PLW-HNO-ENG-00093.doc", null, "Notice_of_proceedings_c6a"),
    NOTICE_OF_HEARING("FL-PLW-HNO-ENG-00525.doc", null, "Notice_of_hearing"),
    SDO("FL-PLW-STD-ENG-00099.doc", null, "standard-directions-order"),
    ORDER_V2("FL-PLW-GOR-ENG-00728.doc", null, "order"),
    EPO("FL-PLW-GOR-ENG-00228.doc", null, "emergency_protection_order"),
    EPO_V2("FL-PLW-GOR-ENG-00744.doc", null, "emergency_protection_order"),
    COVER_DOCS("FL-PLW-LET-ENG-00232.doc", "FL-PLW-LET-WEL-00752.doc", "cover_documents"),
    TRANSLATION_REQUEST("FL-PLW-LET-ENG-00748.doc", null, "translation_request");

    private final String template;
    private final String welshTemplate;

    private final String documentTitle;

    DocmosisTemplates(String template, String welshTemplate, String documentTitle) {
        this.template = template;
        this.welshTemplate = welshTemplate;
        this.documentTitle = documentTitle;
    }

    public String getTemplate() {
        return template;
    }

    public Optional<String> getWelshTemplate() {
        return Optional.ofNullable(welshTemplate);
    }

    public String getDocumentTitle() {
        return documentTitle + ".pdf";
    }

    public String getDocumentTitle(LocalDate date) {
        return String.format("%s_%s.%s", documentTitle, date.format(DateTimeFormatter.ofPattern("ddMMMM")), "pdf");
    }

    public String getTemplate(Language language) {
        if (language == Language.WELSH) {
            return getWelshTemplate().orElse(template);
        }
        return template;
    }
}
