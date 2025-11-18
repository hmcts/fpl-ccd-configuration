import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/caseWithRespodentDetailsAttached.json' assert { type: "json" };
import { newSwanseaLocalAuthorityUserOne, CTSCUser } from "../settings/user-credentials";
import { expect } from "@playwright/test";

test.describe('Change other to respondent', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        expect(caseNumber).toBeDefined();
    });

    test('Change other to respondent @xbrowser',
        async ({ page, signInPage, changeOtherToRespondent }) => {
            casename = 'CTSC changes other to respondent ' + dateTime.slice(0, 10);
            expect(await updateCase(casename, caseNumber, caseData)).toBeTruthy();
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);

            await signInPage.navigateToCaseDetails(caseNumber);
            await changeOtherToRespondent.gotoNextStep('Change other to respondent');
            await changeOtherToRespondent.ChangeOtherToRespondent();
            await changeOtherToRespondent.tabNavigation('People in the case');
            await expect(page.getByText('Respondents 3', { exact: true })).toBeVisible();
            await expect(page.getByText('Thierry', { exact: true })).toBeVisible();
        })
});
