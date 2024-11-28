import {test} from '../fixtures/create-fixture';
import {createCase, updateCase} from "../utils/api-helper";
import caseManagement from '../caseData/caseWithHearingDetails.json' with {type: 'json'};
import {courtAdminBristol, CTSCTeamLeadUser, newSwanseaLocalAuthorityUserOne} from "../settings/user-credentials";
import {expect} from "@playwright/test";
import {testConfig} from "../settings/test-config";


test.describe('Request listing action', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);

    });

    test('CTSC request listing task',
        async ({page, signInPage, listHearingAction}) => {
            caseName = 'CTSC request listing task ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseManagement);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await listHearingAction.gotoNextStep('Request listing action');
            await listHearingAction.requestListing();

            //assertion
            await listHearingAction.tabNavigation('Hearings');

            await expect(page.getByRole('cell', {name: 'Type', exact: true})).toBeVisible();
            await expect(page.getByRole('cell', {name: 'Details', exact: true})).toBeVisible();
            await expect(page.getByRole('cell', {name: 'Date sent', exact: true})).toBeVisible();
            await expect(page.getByText('Listing request', {exact: true})).toBeVisible();
            await expect(page.getByText('Amend/vacate a hearing', {exact: true})).toBeVisible();
            await expect(page.getByText('Special measures required', {exact: true})).toBeVisible();
            if (testConfig.waEnabled) {
                await listHearingAction.tabNavigation('Tasks');
                await listHearingAction.waitForTask('Review listing request (Listing required, Amend/vacate a hearing, Special measures required)');
                await signInPage.logout();
                await signInPage.login(courtAdminBristol.email, courtAdminBristol.password);
                await signInPage.navigateTOCaseDetails(caseNumber);
                await listHearingAction.tabNavigation('Tasks');
                await listHearingAction.reviewListingAction();
            }
        });
})
