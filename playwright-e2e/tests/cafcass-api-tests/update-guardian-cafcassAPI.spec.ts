import {expect, test} from "../../fixtures/fixtures";
import {createCase, updateCase} from "../../utils/api-helper";
import {
    authToken,
    CTSCTeamLeadUser,
    newSwanseaLocalAuthorityUserOne
} from "../../settings/user-credentials";
import submitCase from '../../caseData/mandatorySubmissionFields.json' assert {type: 'json'};
import {cafcassUpdateGuardianDetails, GUARDIAN_DETAILS} from "../../utils/cafcass-api-test-helper";

test.describe('CafcassAPI Update Guardian Details', () => {
    let dateTime = new Date().toISOString();
    let caseNumber: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test.only(' Cafcass user update the guardian details', async ({request, page, signInPage}) => {
        await updateCase('Cafcass update guardian details' + dateTime.slice(0, 10), caseNumber, submitCase);

        let response = await cafcassUpdateGuardianDetails(request, authToken.cafcassAuth, caseNumber, GUARDIAN_DETAILS);

        //assert the response
        expect(response.status()).toBe(200);
        await signInPage.visit();
        await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
        await signInPage.navigateToCaseDetails(caseNumber);
        await signInPage.tabNavigation('People in the case');


        // assert the updated gaurdian details on the screen
        await expect.soft(page.getByText('Guardian 1', {exact: true})).toBeVisible();
        await expect.soft(page.locator('#case-viewer-field-read--guardians')).toContainText('June Thacher');
        await expect.soft(page.locator('#case-viewer-field-read--guardians')).toContainText('june.thacher@mail.com');
        await expect.soft(page.getByText('Guardian 2', {exact: true})).toBeVisible();
        await expect.soft(page.locator('#case-viewer-field-read--guardians')).toContainText('Tom mac');
        await expect.soft(page.locator('#case-viewer-field-read--guardians')).toContainText('tom.mac@mail.com');

    })
    test('Cafcass update guardian details for cases in not valid state', async ({request}) => {

        let response = await cafcassUpdateGuardianDetails(request, authToken.cafcassAuth, caseNumber, GUARDIAN_DETAILS);
        //assertion
        expect(response.status()).toBe(404);
        expect(response.statusText()).toBe('Not Found');
    })
    test('Cafcass Update guardian details by user without cafcass role', async ({request}) => {

        await updateCase('Cafcass Update guardian details by user without cafcass role ' + dateTime.slice(0, 10), caseNumber, submitCase);
        let response = await cafcassUpdateGuardianDetails(request, authToken.systemAuth, caseNumber, GUARDIAN_DETAILS);
        //assertion
        expect(response.status()).toEqual(403);
        expect(response.statusText()).toEqual('Forbidden');

    })

})
