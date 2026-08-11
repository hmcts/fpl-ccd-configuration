import {test} from '../fixtures/create-fixture';
import caseData from '../caseData/mandatorySubmissionFields.json' assert {type: 'json'};
import {CTSCTeamLeadUser, newSwanseaLocalAuthorityUserOne} from '../settings/user-credentials';
import {createCase, updateCase} from "../utils/api-helper";
import { expect } from 'playwright/test';

test.describe('Manage the Retain and Dispose Config', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        expect(caseNumber).toBeDefined();
    });

    test('CTSC suspend case disposal',
        async ({page, signInPage, manageTTL}) => {
            caseName = 'Suspend system case disposal ' + dateTime.slice(0, 10);
            expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await manageTTL.gotoNextStep('Manage Case TTL');
            await manageTTL.suspendTTL();
            await manageTTL.clickContinue();
            await manageTTL.clickSaveAndContinue();
        });

    test('CTSC leader override the system disposal date',
        async ({page, signInPage, manageTTL}) => {
            caseName = 'Override system case disposal date ' + dateTime.slice(0, 10);
            expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await manageTTL.gotoNextStep('Manage Case TTL')
            await manageTTL.overrideSystemTTL();
            await expect.soft(manageTTL.page.getByText('The data entered is not valid for Override TTL')).toBeHidden();
            await manageTTL.clickContinue();
            await manageTTL.clickSaveAndContinue();
            await expect(manageTTL.page.getByText('has been updated with event: Manage Case TTL')).toBeVisible();
        });
});
