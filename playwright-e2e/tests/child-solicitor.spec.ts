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
            await childDetails.addRegisteredSOlOrg();
            await childDetails.clickContinue();
            await childDetails.assignSolicitorToAllChildren();
            await childDetails.clickContinue();
            await childDetails.checkYourAnsAndSubmit();
            await childDetails.tabNavigation('People in the case');
            await expect(page.getByText('Private solicitors', { exact: true })).toHaveCount(4);
            await childDetails.tabNavigation('Change of representatives')
            await expect(page.getByText('Added representative',{ exact: true })).toHaveCount(4);

        });

    test(' @local CTSC user can add different legal representative to each children',
        async ({page, signInPage, childDetails}) => {
            casename = 'CTSC different Child solicitors ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseWithMultipleChild);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await childDetails.gotoNextStep("Children");
            await childDetails.clickContinue();
            await childDetails.addRegisteredSOlOrg();
            await childDetails.clickContinue();
            await childDetails.assignDifferrentChildSolicitor();
            await childDetails.addDifferentSolicitorForChild('Child 1');
            await childDetails.addCafcassSolicitorForChild('Child 2');
            await childDetails.addCafcassSolicitorForChild('Child 3');
            await childDetails.addCafcassSolicitorForChild('Child 4');
            await childDetails.clickContinue();
            await childDetails.checkYourAnsAndSubmit();
            await childDetails.tabNavigation('People in the case');
            await expect(page.getByText('Private solicitors', { exact: true })).toHaveCount(3);
            await expect(page.getByText('FPLSolicitorOrg', { exact: true })).toHaveCount(1);
            await childDetails.tabNavigation('Change of representatives')
            await expect(page.getByText('Added representative',{ exact: true })).toHaveCount(4);
        });

    // this can be handled in the Noc tests.
    test(' CTSC user able to add unregistered solicitor to a child ',
        async ({page, signInPage, childDetails}) => {
            casename = 'CTSC add unregistered child solicitor ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseWithMultipleChild);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await childDetails.gotoNextStep("Children");
            await childDetails.clickContinue();
            await childDetails.addUnregisteredSolOrg();




            //await page.getByLabel('Yes').check();
            // await page.getByLabel('Representative\'s first name').click();
            // await page.getByLabel('Representative\'s first name').fill('ChildSolicitor');
            // await page.getByLabel('Representative\'s first name').press('Tab');
            // await page.getByLabel('Representative\'s last name').fill('UnRegistered');
            // await page.getByLabel('Email address').click();
            // await page.getByLabel('Email address').fill('unregisteredSolicitor@email.com');
            // await page.getByLabel('Organisation name (Optional)').click();
            // await page.getByLabel('Organisation name (Optional)').fill('NewOrganisation');
            // await page.getByRole('textbox', { name: 'Enter a UK postcode' }).click();
            // await page.getByRole('textbox', { name: 'Enter a UK postcode' }).fill('Tw7');
            // await page.getByRole('button', { name: 'Find address' }).click();
            // await page.getByLabel('Select an address').selectOption('12: Object');
            // await page.getByRole('group', { name: 'Telephone number' }).locator('#childrenMainRepresentative_telephoneNumber_telephoneNumber').click();
            // await page.getByRole('group', { name: 'Telephone number' }).locator('#childrenMainRepresentative_telephoneNumber_telephoneNumber').fill('04668789708908');
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
