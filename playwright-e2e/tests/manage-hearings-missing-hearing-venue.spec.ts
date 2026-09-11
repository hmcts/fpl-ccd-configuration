import {test} from '../fixtures/create-fixture';
import caseDataWithValidPreviousHearingVenue
    from '../caseData/caseWithHearingWithValidHearingVenue.json' assert {type: 'json'};
import caseDataWithMissingHearingVenue
    from '../caseData/caseWithHearingWithMissingHearingVenue.json' assert {type: 'json'};
import vacatedHearingCaseData from '../caseData/caseWithVacatedHearing.json' assert {type: 'json'};
import preJudgeAllocationCaseData from '../caseData/casePreAllocationDecision.json' assert {type: 'json'};
import {
    CTSCUser,
    newSwanseaLocalAuthorityUserOne,
    judgeWalesUser
} from "../settings/user-credentials";
import {expect} from "@playwright/test";
import {testConfig} from "../settings/test-config";
import {createCase, updateCase} from "../utils/api-helper";

test.describe('manage hearings', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        expect(caseNumber).toBeDefined();
    });

    test('CTSC admin add new hearing to application with previous hearing venue @xbrowser',
        async ({page, signInPage, manageHearings}) => {
            caseName = 'CTSC manage hearings ' + dateTime.slice(0, 10);
            expect(await updateCase(caseName, caseNumber, caseDataWithValidPreviousHearingVenue)).toBeTruthy();
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await manageHearings.gotoNextStep('Manage hearings');

            await manageHearings.selectHearingOption('Add a new hearing');
            await manageHearings.clickContinue();
            await expect.soft(manageHearings.page.getByText('NOTE: For applications and any interim directions select "Further Case Management')).toBeVisible();

            await manageHearings.selectHearingType('Case management');
            await manageHearings.assertLastHearingVenue('Birmingham Social Security and Child Support Tribunal, PO Box 14620, Administrative Support Centre, Birmingham, B16 6FR');

            await manageHearings.selectHearingVenue('Yes');
            await manageHearings.selectHearingAttence('In person');

            await manageHearings.enterHearingDate();
            await manageHearings.enterHearingLength();
            await manageHearings.clickContinue();
            await manageHearings.selectAllocatedJusticeAsHearingJudge();
            await manageHearings.clickContinue();
            await manageHearings.sendHearingNotifications();
            await manageHearings.clickContinue();
            await manageHearings.checkYourAnsAndSubmit();
            await expect(page.getByText('has been updated with event: Manage hearings')).toBeVisible();
            await manageHearings.tabNavigation('Hearings');
            await expect(manageHearings.page.getByText('Birmingham SS&CS Tribunal', {exact: true})).toHaveCount(2);

        });
    test('CTSC admin add new hearing to application with missing hearing venue @xbrowser',
        async ({page, signInPage, manageHearings}) => {
            caseName = 'CTSC manage hearings ' + dateTime.slice(0, 10);
            expect(await updateCase(caseName, caseNumber, caseDataWithMissingHearingVenue)).toBeTruthy();
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await manageHearings.gotoNextStep('Manage hearings');
            await manageHearings.selectHearingOption('Add a new hearing');
            await manageHearings.clickContinue();
            expect(await manageHearings.page.locator('ccd-field-read-label', {hasText: 'Last court'})).toBeHidden();
            await manageHearings.selectHearingType('Judgment after hearing');
            await manageHearings.selectHearingVenue('No')
            await manageHearings.selectHearingAttence('In person');
            await manageHearings.enterHearingDate();
            await manageHearings.enterHearingLength();
            await manageHearings.clickContinue();
            await manageHearings.selectAllocatedJusticeAsHearingJudge();
            await manageHearings.clickContinue();
            await manageHearings.sendHearingNotifications();
            await manageHearings.clickContinue();
            await manageHearings.checkYourAnsAndSubmit();
            await expect(page.getByText('has been updated with event: Manage hearings')).toBeVisible();
            await manageHearings.tabNavigation('Hearings');
            await expect(manageHearings.page.getByText('Judgment after hearing', {exact: true})).toBeVisible()
            await expect(manageHearings.page.getByText('Swansea Crown Court', {exact: true})).toBeVisible()
        }
    )


});
