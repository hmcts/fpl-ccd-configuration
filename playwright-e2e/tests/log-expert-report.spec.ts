import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/mandatorySubmissionFields.json' with { type: "json" };
import returnedCase from '../caseData/returnCase.json' with { type: "json" };
import { CTSCUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser } from "../settings/user-credentials";
import { expect } from "@playwright/test";

test.describe('log expert report', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('log expert report',
        async ({ page, signInPage, logExpertReport }) => {
            caseName = 'CTSC log expert report ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);

            await logExpertReport.gotoNextStep('Log expert report');
            await logExpertReport.logExpertReport();
            await expect(page.getByText('Log expert report')).toBeVisible();
          })
});
