package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

class SubmittedFormFilenameHelperTest {

    @Test
    void fileNameShouldContainCaseReferenceWhenNoCaseNameIsProvided() throws IOException {
        CaseDetails caseDetails = emptyCaseDetails();

        String fileName = SubmittedFormFilenameHelper.buildFileName(caseDetails);

        assertThat(fileName).isEqualTo("123.pdf");
    }

    @Test
    void fileNameShouldContainCaseTitleWhenProvided() throws IOException {
        CaseDetails caseDetails = populatedCaseDetails();

        String fileName = SubmittedFormFilenameHelper.buildFileName(caseDetails);

        assertThat(fileName).isEqualTo("test.pdf");
    }
}
