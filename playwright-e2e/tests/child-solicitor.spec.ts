import { test } from '../fixtures/create-fixture';
import { Apihelp } from '../utils/api-helper';
import caseWithMaxChildern from '../caseData/caseWithMaxChildren.json';
import caseWithChildrenCafcassSolicitor from '../caseData/caseWithMultipleChildCafcassSolicitor.json'

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
            await apiDataSetup.updateCase(casename, caseNumber, caseWithMaxChildern);
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

    test(' CTSC user can add different legal representative to each children',
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

    test('CTSC user able to add unregistered solicitor to a child ',
        async ({page, signInPage, childDetails}) => {
            casename = 'CTSC add unregistered child solicitor ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseWithChildrenCafcassSolicitor);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await childDetails.gotoNextStep("Children");
            await childDetails.clickContinue();
            await childDetails.addUnregisteredSolOrg();
            await childDetails.clickContinue();
            await childDetails.assignSolicitorToAllChildren();
            await childDetails.clickContinue();
            await childDetails.checkYourAnsAndSubmit();
            await childDetails.tabNavigation('People in the case');
            await expect(page.locator('#case-viewer-field-read--children1')).toContainText('Organisation (unregistered)');
            await expect(page.locator('#case-viewer-field-read--children1')).toContainText('NewOrganisation');
            await expect(page.getByRole('tab', { name: 'Change of representatives' })).toBeHidden();
        });

    test('@local CTSC user remove child solicitors',
        async ({page, signInPage, childDetails}) => {
            casename = 'CTSC change child solicitor ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseWithChildrenCafcassSolicitor);
            console.log(caseNumber);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await childDetails.gotoNextStep("Children");
            await page.pause();
            await childDetails.clickContinue();
            await childDetails.clickContinue();


        });
});
