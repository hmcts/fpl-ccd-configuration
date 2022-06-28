package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.base.Strings;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C110A;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

public class SubmittedFormFilenameHelper {

    private SubmittedFormFilenameHelper() {
        // NO-OP
    }

    public static String buildFileName(final CaseData caseData, final boolean isDraft) {
        String caseName = Strings.nullToEmpty(caseData.getCaseName()).trim();

        if (isDraft) {
            return format("draft_%s", format(C110A.getDocumentTitle(),
                formatLocalDateToString(now(), "ddMMM").toLowerCase()));
        }

        if (!Strings.isNullOrEmpty(caseName)) {
            return caseName.replaceAll("\\s", "_") + ".pdf";
        }

        return caseData.getId() + ".pdf";
    }
}
