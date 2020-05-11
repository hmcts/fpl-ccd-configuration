package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

class SubmittedFormFilenameHelperTest {

    @Test
    void fileNameShouldContainCaseReferenceWhenNoCaseNameIsProvidedAndNotDraftApplication() {
        CaseDetails caseDetails = emptyCaseDetails();

        String fileName = SubmittedFormFilenameHelper.buildFileName(caseDetails, false);

        assertThat(fileName).isEqualTo("123.pdf");
    }

    @Test
    void fileNameShouldContainCaseTitleWhenProvidedAndNotDraftApplication() {
        CaseDetails caseDetails = populatedCaseDetails();

        String fileName = SubmittedFormFilenameHelper.buildFileName(caseDetails, false);

        assertThat(fileName).isEqualTo("test.pdf");
    }

    @Test
    void fileShouldMatchDraftApplicationFilenameWhenAndApplicationIsDraft() {
        CaseDetails caseDetails = populatedCaseDetails();

        String fileName = SubmittedFormFilenameHelper.buildFileName(caseDetails, true);

        assertThat(fileName)
            .isEqualTo(format("draft_c110a_application_%s.pdf",
                formatLocalDateToString(now(), "ddMMM").toLowerCase()));
    }
}
