package uk.gov.hmcts.reform.fpl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DocumentService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminManagesHearingsApiTest extends AbstractApiTest {

    public static final String INPUT_FILE = "admin-manage-hearings/input-case-details.json";
    // public static final String EXPECTED_FILE = "admin-manage-hearings/%s/expected.txt";
    private final LocalDate todaysDate = LocalDate.now();
    private final LocalDateTime currentDateTime = LocalDateTime.now();
    private CaseData startingCaseData;

    @Autowired
    private DocumentService documentService;

    @Test
    public void adminCreatesFirstHearing() {
        parametrizedTests("c32a");
    }

    public void parametrizedTests(String inputFileDirectory) {
        startingCaseData = createCase(INPUT_FILE, LA_SWANSEA_USER_1);
        callAboutToSubmit(startingCaseData);
    }

    private void callAboutToSubmit(CaseData caseData) {
        CaseData updatedCase = caseData.toBuilder()
            .hearingOption(HearingOptions.NEW_HEARING)
            .hearingType(HearingType.CASE_MANAGEMENT)
            .build();

        CallbackResponse response = callback(updatedCase, COURT_ADMIN, "manage-hearings/about-to-submit");

        assertThat(response.getCaseData().getHearingDetails()).doesNotContainNull();

        var test = response.getCaseData();
    }
}
