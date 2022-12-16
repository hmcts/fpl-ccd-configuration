package uk.gov.hmcts.reform.fpl.e2e;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.springframework.util.ObjectUtils.isEmpty;

public class AddCaseNoteE2ETest extends AbstractE2ETest {

    static final String CASE_NOTE = "This is a case note!";

    Long caseId;

    @Before
    public void setupCase() {
        if (isEmpty(caseId)) {
            CaseData caseData = createCase("order-generation/case.json", LA_SWANSEA_USER_1);
            caseId = caseData.getId();
        }
    }

    @Test
    public void hmctsAdminAddsACaseNote() {
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
