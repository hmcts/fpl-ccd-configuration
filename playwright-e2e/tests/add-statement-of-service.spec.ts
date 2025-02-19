import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/caseWithRespodentDetailsAttached.json' assert { type: "json" };
import { newSwanseaLocalAuthorityUserOne, HighCourtAdminUser } from "../settings/user-credentials";
import { expect } from "@playwright/test";

test.describe('Add statement of service', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });
    test('Add statement of service',
        async ({ page, signInPage, addStatementOfService }) => {
            caseName = 'LA Add statement of service' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password,);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await addStatementOfService.gotoNextStep('Add statement of service (c9)');
            await addStatementOfService.gotoNextStep('Add statement of service (c9)');
            await addStatementOfService.UploadAddStatementOfService();
            await expect(page.getByText('Add statement of service')).toBeVisible();
        })
});
