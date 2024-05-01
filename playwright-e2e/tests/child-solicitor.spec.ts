import { test } from '../fixtures/create-fixture';
import { Apihelp } from '../utils/api-helper';
import caseWithMaxChilder from '../caseData/caseWithMaxChildren.json';
import caseWithMultipleChild from '../caseData/mandatorySubmissionFields.json'
import {
    newSwanseaLocalAuthorityUserOne,
    privateSolicitorOrgUser,
    FPLSolicitorOrgUser,
    CTSCTeamLeadUser
} from '../settings/user-credentials';
import { expect } from '@playwright/test';

test.describe('Manage child solicitor representatives ', () => {
    let apiDataSetup = new Apihelp();
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;
    test.beforeEach(async () => {
        caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test(' Child solicitor can get access to the case only after submitted state',
        async ({page, signInPage, childDetails}) => {
            casename = '' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseWithMaxChilder);
            await signInPage.visit();
            await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password)
            page.pause();
           // await signInPage.navigateTOCaseDetails(caseNumber);
            //got NOC and add the child details
            await page.getByRole('link', { name: 'Notice of change' }).click();
            await page.getByLabel('Online case reference number').click();
            await page.getByLabel('Online case reference number').fill(caseNumber);
            await page.getByRole('button', { name: 'Continue' }).click();
            // await expect(page.locator('#heading-something-wrong')).toContainText('Something went wrong');
            await page.getByText('Your notice of change request').click();
            // await expect(page.locator('exui-noc-fill-form-offline')).toContainText('Your notice of change request has not been submitted.');

        });

    test('CTSC user can add one legal representative to all children ',
        async ({page, signInPage, childDetails}) => {
            casename = 'CTSC add one solicitor to represent all children ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseWithMultipleChild);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await childDetails.gotoNextStep("Children");
            await childDetails.clickContinue();
            await childDetails.AddSingleSolicitor();
            await childDetails.clickContinue();
            await page.pause();
            await childDetails.newassignToAllChild();
            await childDetails.clickContinue();
            await childDetails.checkYourAnsAndSubmit();
            await childDetails.tabNavigation('People in the case');
            await expect(page.getByText('Private solicitors', { exact: true })).toHaveCount(4);
            await childDetails.tabNavigation('Change of representatives')
            await expect(page.getByText('Added representative',{ exact: true })).toHaveCount(4);

        });

    test(' CTSC user can add different legal representative to each children',
        async ({page, signInPage, legalCounsel}) => {
            casename = 'CTSC add multiple Child solicitor ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseWithMultipleChild);
            console.log("casenumber " + caseNumber);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await page.pause();
            await page.getByLabel('Next step').selectOption('12: Object');
            await page.getByRole('button', { name: 'Go' }).click();
            await page.getByRole('button', { name: 'Continue' }).click();
            await page.getByLabel('Yes').check();
            await page.getByLabel('Representative\'s first name').click();
            await page.getByLabel('Representative\'s first name').fill('child ');
            await page.getByLabel('Representative\'s first name').press('Tab');
            await page.getByLabel('Representative\'s last name').fill('solicitor1');
            await page.getByLabel('Representative\'s last name').press('Tab');
            await page.getByLabel('Email address').fill('private.solictor@mailinator.com');
            await page.getByLabel('You can only search for').click();
            await page.getByLabel('You can only search for').fill('private');
            await page.getByTitle('Select the organisation Private solicitors', { exact: true }).click();
            await page.getByRole('group', { name: 'Telephone number' }).locator('#childrenMainRepresentative_telephoneNumber_telephoneNumber').click();
            await page.getByRole('group', { name: 'Telephone number' }).locator('#childrenMainRepresentative_telephoneNumber_telephoneNumber').fill('845789900');
            await page.getByRole('button', { name: 'Continue' }).click();
            await page.getByRole('radio', { name: 'No' }).check();
            await page.getByRole('group', { name: 'Child 1' }).getByLabel('Representative\'s first name (').click();
            await page.getByRole('group', { name: 'Child 1' }).getByLabel('Representative\'s first name (').click();
            await page.getByRole('group', { name: 'Child 1' }).getByLabel('Representative\'s first name (').fill('child1');
            await page.getByRole('group', { name: 'Child 1' }).getByLabel('Representative\'s first name (').press('Tab');
            await page.getByRole('group', { name: 'Child 1' }).getByLabel('Representative\'s last name (').fill('private solicitor');
            await page.getByLabel('You can only search for').click();
            await page.getByLabel('You can only search for').fill('fpls');
            await page.getByTitle('Select the organisation FPLSolicitorOrg').click();
            await page.getByRole('group', { name: 'Child 2' }).getByLabel('Yes').check();
            await page.getByRole('group', { name: 'Child 3' }).getByLabel('Yes').check();
            await page.getByRole('group', { name: 'Child 4' }).getByLabel('Yes').check();
            await page.getByRole('button', { name: 'Continue' }).click();
            await page.getByRole('textbox', { name: 'Email address (Optional)' }).click();
            await page.getByRole('textbox', { name: 'Email address (Optional)' }).fill('privatechild1solicitor@email.com');
            await page.getByRole('button', { name: 'Continue' }).click();
            await page.getByRole('button', { name: 'Save and continue' }).click();
            await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1714465774935316');
            await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1714465774935316#Summary');
            await page.getByRole('tab', { name: 'People in the case' }).locator('div').click();
            await page.getByRole('cell', { name: 'FPLSolicitorOrg', exact: true }).click();
            // await expect(page.getByText('FPLSolicitorOrg')).toBeVisible();
            // await expect(page.locator('#case-viewer-field-read--children1')).toContainText('FPLSolicitorOrg');
            // await expect(page.locator('#case-viewer-field-read--children1')).toContainText('Private solicitors');
            await page.getByText('Private solicitors', { exact: true }).nth(1).click();
            // await expect(page.getByText('Private solicitors', { exact: true }).nth(1)).toBeVisible();

        });

    // this can be handled in the Noc tests.
    test(' CTSC user able to add unregistered solicitor to a child ',
        async ({page, signInPage, legalCounsel}) => {
            casename = 'Child solicitor access case by NoC ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseWithMultipleChild);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await page.pause();
            await page.getByLabel('Next step').selectOption('12: Object');
            await page.getByRole('button', { name: 'Go' }).click();
            await page.locator('ccd-case-event-trigger div').filter({ hasText: 'ChildrenChild solicitor' }).click();
            await page.getByRole('button', { name: 'Continue' }).click();
            await page.getByLabel('Yes').check();
            await page.getByLabel('Representative\'s first name').click();
            await page.getByLabel('Representative\'s first name').fill('ChildSolicitor');
            await page.getByLabel('Representative\'s first name').press('Tab');
            await page.getByLabel('Representative\'s last name').fill('UnRegistered');
            await page.getByLabel('Email address').click();
            await page.getByLabel('Email address').fill('unregisteredSolicitor@email.com');
            await page.getByLabel('Organisation name (Optional)').click();
            await page.getByLabel('Organisation name (Optional)').fill('NewOrganisation');
            await page.getByRole('textbox', { name: 'Enter a UK postcode' }).click();
            await page.getByRole('textbox', { name: 'Enter a UK postcode' }).fill('Tw7');
            await page.getByRole('button', { name: 'Find address' }).click();
            await page.getByLabel('Select an address').selectOption('12: Object');
            await page.getByRole('group', { name: 'Telephone number' }).locator('#childrenMainRepresentative_telephoneNumber_telephoneNumber').click();
            await page.getByRole('group', { name: 'Telephone number' }).locator('#childrenMainRepresentative_telephoneNumber_telephoneNumber').fill('04668789708908');
            await page.getByRole('button', { name: 'Continue' }).click();
            await page.getByRole('radio', { name: 'Yes' }).check();
            await page.getByRole('button', { name: 'Continue' }).click();
            await page.getByRole('button', { name: 'Save and continue' }).click();
            await page.getByRole('tab', { name: 'People in the case' }).click();
            // await expect(page.locator('#case-viewer-field-read--children1')).toContainText('Organisation (unregistered)');
            // await expect(page.locator('#case-viewer-field-read--children1')).toContainText('NewOrganisation');
            await page.locator('mat-tab-header button').nth(1).dblclick();
            await page.locator('mat-tab-header button').first().dblclick();

        });
    test('Change of child solicitor ',
        async ({page, signInPage, legalCounsel}) => {
            casename = 'CTSC change child solicitor ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseWithMultipleChild);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await page.pause();

        });
});
