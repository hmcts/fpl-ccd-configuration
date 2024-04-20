import { test } from '../fixtures/create-fixture';
import { Apihelp } from '../utils/api-helper';
import caseData from '../caseData/mandatoryWithMultipleChildren.json';
import {newSwanseaLocalAuthorityUserOne, CTSCUser} from '../settings/user-credentials';
import { expect } from "@playwright/test";

test.describe('Add a case flag', () => {
    let apiDataSetup = new Apihelp();
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('Add and remove a case flag',
        async ({ page, signInPage, addAdminCaseFlag }) => {
            caseName = 'Add and remove a case flag' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await addAdminCaseFlag.gotoNextStep('Add case flag');
            await addAdminCaseFlag.addCaseFlag();
            await addAdminCaseFlag.tabNavigation('Summary');
            await expect(page.getByText('Potentially violent person')).toBeVisible();
            await expect(page.getByText('additional notes')).toBeVisible();
            await addAdminCaseFlag.removeCaseFlag();
            await addAdminCaseFlag.tabNavigation('Summary');
            await expect(page.getByText('Potentially violent person')).toHaveCount(0);
            await expect(page.getByText('additional notes')).toHaveCount(0);
        });

});

