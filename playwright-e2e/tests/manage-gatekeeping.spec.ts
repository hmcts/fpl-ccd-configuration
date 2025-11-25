import {test} from '../fixtures/create-fixture';
import {createCase, updateCase} from "../utils/api-helper";
import submittedCase from '../caseData/mandatorySubmissionFields.json' assert {type: "json"};
import {
    newSwanseaLocalAuthorityUserOne,
    CTSCUser
} from "../settings/user-credentials";
import {expect} from "@playwright/test";


test.describe('Adding gatekeeping details', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        expect(caseNumber).toBeDefined();
    });

    test('CTSC user add gatekeeping details @xbrowser',
        async ({
                    signInPage, familymanCaseNumber, sendToGatekepperPage,
                   caseFileView
               }) => {
            caseName = 'Gatekeeping details ' + dateTime.slice(0, 10);
            expect(await updateCase(caseName, caseNumber, submittedCase)).toBeTruthy();
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);

            await test.step('Add family man reference number', async () => {
                await familymanCaseNumber.gotoNextStep('Add case number');
                await familymanCaseNumber.fillfamilyManCaseNumber('TN24C51337');
                await familymanCaseNumber.clickSubmit();

                await expect(familymanCaseNumber.page.getByText('has been updated with event: Add case number')).toBeVisible();
                await expect(familymanCaseNumber.page.getByRole('heading', {
                    level: 2,
                    name: 'TN24C51337'
                })).toBeVisible();

                await sendToGatekepperPage.tabNavigation('History');
                expect(await sendToGatekepperPage.page.getByRole('table', {name: 'History'}).locator('tr').nth(1).innerText()).toContain('Add case number');

            });
            await test.step('Send to gatekeeper', async () => {
                await signInPage.gotoNextStep('Send to gatekeeper');
                await sendToGatekepperPage.fillGateKeeperEmail('gatekeeper@email.com');
                await sendToGatekepperPage.clickSubmit();
                await sendToGatekepperPage.clickSaveAndContinue();

                await sendToGatekepperPage.tabNavigation('History');
                const endState = await sendToGatekepperPage.getCellValueInTable('Details', 'End state');
                expect(endState).toBe('Gatekeeping')
                expect(await sendToGatekepperPage.page.getByRole('table', {name: 'History'}).locator('tr').nth(1).innerText()).toContain('Send to gatekeeper');

            });
        });
});
