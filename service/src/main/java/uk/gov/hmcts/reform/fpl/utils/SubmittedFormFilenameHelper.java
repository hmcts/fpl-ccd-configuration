package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.base.Strings;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

public class SubmittedFormFilenameHelper {
    private static final String DAY_MONTH_FORMAT = "ddMMM";

    private SubmittedFormFilenameHelper() {
        // NO-OP
    }

    public static String buildFileName(final CaseData caseData, final boolean isDraft, DocmosisTemplates template) {
        String caseName = Strings.nullToEmpty(caseData.getCaseName()).trim();

        if (isDraft) {
            return format("draft_%s", format(template.getDocumentTitle(),
                formatLocalDateToString(now(), DAY_MONTH_FORMAT).toLowerCase()));
        }

        if (!Strings.isNullOrEmpty(caseName)) {
            return caseName.replaceAll("\\s", "_") + ".pdf";
        }

        return caseData.getId() + ".pdf";
    }

    public static String buildGenericFileName(final boolean isDraft, DocmosisTemplates template) {
        if (isDraft) {
            return format("draft_%s", format(template.getDocumentTitle(),
                formatLocalDateToString(now(), DAY_MONTH_FORMAT).toLowerCase()));
        } else {
            return format(template.getDocumentTitle(),
                formatLocalDateToString(now(), DAY_MONTH_FORMAT).toLowerCase());
        }
    }
}
