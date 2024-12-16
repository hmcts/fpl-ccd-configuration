import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: "json" };
import { CTSCTeamLeadUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser, CTSCUser } from "../settings/user-credentials";
import { expect } from "@playwright/test";

test.describe('Manage LAs / Transfer to court', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('CTSC transfer to a new court and submit case',
        async ({ page, signInPage, manageLaTransferToCourts }) => {
            caseName = 'CTSC transfers case' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');
            await manageLaTransferToCourts.updateManageLaTransferToCourts();
            await manageLaTransferToCourts.tabNavigation('People in the case');
            await expect(page.getByText('Designated local authority')).toBeVisible();

        })
    test('CTSC gives access to another local authority',
        async ({ page, signInPage, manageLaTransferToCourts }) => {
            caseName = 'CTSC gives access to another Local authority' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');
            await manageLaTransferToCourts.updateCourtAccess();
            await manageLaTransferToCourts.tabNavigation('People in the case');
            await expect(page.getByText('Applicant 2')).toBeVisible();

        })
    test('CTSC removes access',
        async ({ page, signInPage, manageLaTransferToCourts }) => {
            caseName = 'CTSC removed access' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');
            await manageLaTransferToCourts.updateCourtAccess();
            await manageLaTransferToCourts.tabNavigation('People in the case');
            await expect(page.getByText('Applicant 1')).toBeVisible();

            await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');
            await manageLaTransferToCourts.updateRemoveAccess();
            await manageLaTransferToCourts.tabNavigation('People in the case');
            await expect(page.getByText('Applicant 1')).toBeVisible();
        })
    test('CTSC tranfers to another local authority',
        async ({ page, signInPage, manageLaTransferToCourts }) => {
            caseName = 'CTSC transfers to another local authority' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');
            await manageLaTransferToCourts.updateTranferToLa();
            await manageLaTransferToCourts.tabNavigation('People in the case');
            await expect(page.getByText('Designated local authority')).toBeVisible();
        })
});
