package uk.gov.hmcts.reform.fpl.enums;

import java.util.HashMap;
import java.util.Map;

public enum DocmosisTemplates {
    C6("C6.docx", "Notice_of_proceedings_(c6)", ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_PARTIES),
    C6A("C6a.docx", "Notice_of_proceedings_(c6a)", ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES);

    private final String template;
    private final String documentTitle;

    private interface Inner {
        Map<ProceedingType, DocmosisTemplates> docmosisTemplateToProceedingType = new HashMap<>();
    }

    DocmosisTemplates(String template, String documentTitle, ProceedingType proceedingType) {
        this.template = template;
        this.documentTitle = documentTitle;
        Inner.docmosisTemplateToProceedingType.put(proceedingType, this);
    }

    public static DocmosisTemplates getFromProceedingType(ProceedingType label) {
        return Inner.docmosisTemplateToProceedingType.get(label);
    }

    public String getTemplate() {
        return template;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }
}
