package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseData;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

class SubmittedFormFilenameHelperTest {
    private static CaseData caseData;

    @BeforeAll
    static void setup() {
        caseData = populatedCaseData();
    }

    @Test
    void filenameShouldContainCaseReferenceWhenNoCaseNameIsProvidedAndNotDraftApplication() {
        String fileName = SubmittedFormFilenameHelper.buildFileName(emptyCaseData(), false);

        assertThat(fileName).isEqualTo("123.pdf");
    }

    @Test
    void filenameShouldContainCaseTitleWhenProvidedAndNotDraftApplication() {
        String fileName = SubmittedFormFilenameHelper.buildFileName(caseData, false);

        assertThat(fileName).isEqualTo("test.pdf");
    }

    @Test
    void filenameShouldContainDraftApplicationAndCurrentDayWithMonthSuffixedWhenApplicationIsDraft() {
        String fileName = SubmittedFormFilenameHelper.buildFileName(caseData, true);

        assertThat(fileName)
            .isEqualTo(format("draft_c110a_application_%s.pdf",
                formatLocalDateToString(now(), "ddMMM").toLowerCase()));
    }
}
