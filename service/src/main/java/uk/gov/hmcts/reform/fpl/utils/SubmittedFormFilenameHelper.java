package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.base.Strings;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

public class SubmittedFormFilenameHelper {

    private SubmittedFormFilenameHelper() {
        // NO-OP
    }

    public static String buildFileName(final CaseData caseData, final boolean isDraft) {
        return buildFileName(caseData, isDraft, false);
    }


    public static String buildFileName(final CaseData caseData, final boolean isDraft, boolean isC1) {
        String caseName = Strings.nullToEmpty(caseData.getCaseName()).trim();

        if (isDraft) {
            String documentTitle = isC1 ? "c1_application_%s.pdf" : "c110a_application_%s.pdf";
            return format("draft_%s", format(documentTitle,
                formatLocalDateToString(now(), "ddMMM").toLowerCase()));
        }

        if (!Strings.isNullOrEmpty(caseName)) {
            return caseName.replaceAll("\\s", "_") + ".pdf";
        }

        return caseData.getId() + ".pdf";
    }

    public static String buildGenericFileName(final boolean isDraft, DocmosisTemplates template) {
        if (isDraft) {
            return format("draft_%s", format(template.getDocumentTitle(),
                formatLocalDateToString(now(), "ddMMM").toLowerCase()));
        } else {
            return format(template.getDocumentTitle(),
                formatLocalDateToString(now(), "ddMMM").toLowerCase());
        }
    }
}
