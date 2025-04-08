import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/caseSentToGatekeeper.json' assert { type: "json" };
import { newSwanseaLocalAuthorityUserOne, CTSCUser} from "../settings/user-credentials";
import { expect } from "@playwright/test";
import {LAUserPage} from "../pages/local-authority-user-browser.ts";

test.describe('Case progression report ', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
	});

	test('Case progression report',
		async ({ page, signInPage, caseProgressionReport,ctscUser }) => {
			caseName = 'CTSC upload case progression details' + dateTime.slice(0, 10);
			await updateCase(caseName, caseNumber, caseData);
            await caseProgressionReport.switchUser(ctscUser.page);
			// await signInPage.visit();
			// await signInPage.login(CTSCUser.email, CTSCUser.password);
			await caseProgressionReport.navigateTOCaseDetails(caseNumber);

			await caseProgressionReport.gotoNextStep('Case progression report');
			await caseProgressionReport.CaseProgressionReport();
			await expect(caseProgressionReport.page.getByText('Case Progression report')).toBeVisible();
		})
});
