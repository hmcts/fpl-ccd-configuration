package uk.gov.hmcts.reform.fpl.e2e;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.api.ApiTestService;
import uk.gov.hmcts.reform.fpl.e2e.pages.CaseDetailsPage;
import uk.gov.hmcts.reform.fpl.e2e.pages.LoginPage;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.util.TestConfiguration;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.api.ApiTestService.COURT_ADMIN;
import static uk.gov.hmcts.reform.fpl.api.ApiTestService.LA_SWANSEA_USER_1;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AddCaseNoteE2ETest extends AbstractE2ETest {

    final ApiTestService apiTestService;
    final LoginPage loginPage;
    final CaseDetailsPage caseDetailsPage;
    final TestConfiguration testConfiguration;

    static final String CASE_NOTE = "This is a case note!";

    Long caseId;

    @BeforeEach
    void setupCase() {
        if (isEmpty(caseId)) {
            CaseData caseData = apiTestService.createCase("fixtures/mandatorySubmissionFields.json",
                LA_SWANSEA_USER_1, "e2e test - add case note");
            caseId = caseData.getId();
        }
    }

    @Test
    void hmctsAdminAddsACaseNote() {
        // Login as Court admin
        loginPage.login(page, COURT_ADMIN);

        // Go to the case
        page.navigate(testConfiguration.getXuiUrl() + "/cases/case-details/" + caseId);

        caseDetailsPage.startEvent(page, "Add a case note");

        page.fill("#caseNote", CASE_NOTE);

        goToNextPage();
        saveAndContinue();

        openTab(caseId, "Notes");
        page.waitForSelector("text=" + CASE_NOTE);
    }
}
