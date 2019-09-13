package uk.gov.hmcts.reform.fpl.enums;

import java.util.EnumMap;
import java.util.Map;

public enum DocmosisTemplates {
    C6("C6.docx", "Notice_of_proceedings_(c6)", ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_PARTIES),
    C6A("C6a.docx", "Notice_of_proceedings_(c6a)", ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES);

    private final String template;
    private final String documentTitle;
    private final ProceedingType proceedingType;

    private static Map<ProceedingType, DocmosisTemplates> PROCEEDING_TYPE_TO_DOCMOSIS_TEMPLATE =
        new EnumMap<>(ProceedingType.class);

    static {
        for (DocmosisTemplates docmosisTemplatee : DocmosisTemplates.values()) {
            PROCEEDING_TYPE_TO_DOCMOSIS_TEMPLATE.put(docmosisTemplatee.proceedingType, docmosisTemplatee);
        }
    }

    DocmosisTemplates(String template, String documentTitle, ProceedingType proceedingType) {
        this.template = template;
        this.documentTitle = documentTitle;
        this.proceedingType = proceedingType;
    }

    public static DocmosisTemplates getFromProceedingType(ProceedingType label) {
        return PROCEEDING_TYPE_TO_DOCMOSIS_TEMPLATE.get(label);
    }

    public String getTemplate() {
        return template;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }
}
