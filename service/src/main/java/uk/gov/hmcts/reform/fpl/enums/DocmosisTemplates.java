package uk.gov.hmcts.reform.fpl.enums;

import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public enum DocmosisTemplates {
    C110A("FL-PLW-APP-ENG-00095.doc", "FL-PLW-APP-WEL-00753.doc", "c110a_application_%s"),
    C1("FL-PLW-APP-ENG-01084.doc", null, "c1_application_%s"),
    C14_SUPPLEMENT("FL-PLW-APP-ENG-01086.doc", null, "c14_supplement_%s"),
    C15_SUPPLEMENT("FL-PLW-APP-ENG-01088.doc", null, "c15_supplement_%s"),
    C16_SUPPLEMENT("FL-PLW-APP-ENG-01083.doc", null, "c16_supplement_%s"),
    C17_SUPPLEMENT("FL-PLW-APP-ENG-01089.doc", null, "c17_supplement_%s"),
    C18_SUPPLEMENT("FL-PLW-APP-ENG-01087.doc", null, "c18_supplement_%s"),
    C20_SUPPLEMENT("FL-PLW-APP-ENG-01085.doc", null, "c20_supplement_%s"),
    C6("FL-PLW-HNO-ENG-00092.doc", null, "Notice_of_proceedings_c6"),
    C6A("FL-PLW-HNO-ENG-00093.doc", null, "Notice_of_proceedings_c6a"),
    NOTICE_OF_HEARING("FL-PLW-HNO-ENG-00525.doc", null, "Notice_of_hearing"),
    NOTICE_OF_HEARING_VACATED("FL-PLW-HNO-ENG-notice-of-hearing-vacated.doc", null,
        "Notice_of_hearing_vacated"),
    SDO("FL-PLW-STD-ENG-00099-v2.doc", null, "standard-directions-order"),
    UDO("FL-PLW-STD-ENG-00099-v2.doc", null, "urgent-directions-order"),
    ORDER_V2("FL-PLW-GOR-ENG-00728.doc", null, "order"),
    EPO("FL-PLW-GOR-ENG-00228.doc", null, "emergency_protection_order"),
    EPO_V2("FL-PLW-GOR-ENG-00744.doc", null, "emergency_protection_order"),
    COVER_DOCS("FL-PLW-LET-ENG-00232.doc", "FL-PLW-LET-WEL-00752.doc", "cover_documents"),
    TRANSLATION_REQUEST("FL-PLW-LET-ENG-00748.doc", null, "translation_request"),
    A70("FL-PLW-GOR-ENG-00763V2.doc", null, "placement_order_a70"),
    A81("FL-PLW-GOR-ENG-00728.doc", null, "placement_order_a81"),
    A206("FL-PLW-LET-ENG-00768.doc", null, "placement_order_notification_a206"),
    A92("FL-PLW-HNO-ENG-notice-of-placement.doc", null, "notice_of_hearing_placement"),
    HIGH_COURT_SEAL("FL-PLW-ASS-ENG-HIGH-COURT-SEAL.docx", null, "high_court_seal");

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

    public String getTemplate(Language language) {
        if (language == Language.WELSH) {
            return getWelshTemplate().orElse(template);
        }
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

}
