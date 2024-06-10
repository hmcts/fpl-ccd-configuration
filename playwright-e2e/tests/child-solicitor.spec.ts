import { test } from '../fixtures/create-fixture';
import {createCase, giveAccessToCase, updateCase} from "../utils/api-helper";
import caseWithChildrenCafcassSolicitor from '../caseData/caseWithMultipleChildCafcassSolicitor.json' assert { type: "json" }
import caseWithMultipleChild from '../caseData/mandatorySubmissionFields.json' assert { type: "json" }
import {
    newSwanseaLocalAuthorityUserOne,
    privateSolicitorOrgUser,
    FPLSolicitorOrgUser,
    CTSCTeamLeadUser
} from '../settings/user-credentials';
import { expect } from '@playwright/test';

test.describe('Manage child representatives ', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('CTSC user can add one legal representative to all children ',
        async ({page, signInPage, childDetails}) => {
            casename = 'CTSC add one solicitor to represent all children ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseWithMultipleChild);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await childDetails.gotoNextStep("Children");
            await childDetails.continueButton.click();
            await childDetails.addRegisteredSOlOrg();
            await childDetails.continueButton.click();
            await childDetails.assignSolicitorToAllChildren();
            await childDetails.continueButton.click();
            await childDetails.checkYourAnsAndSubmit();
            await childDetails.tabNavigation('People in the case');
            await expect(page.getByText('Private solicitors', {exact: true})).toHaveCount(4);
            await childDetails.tabNavigation('Change of representatives')
            await expect(page.getByText('Added representative', {exact: true})).toHaveCount(4);
        });

    test(' CTSC user can add different legal representative to each children',
        async ({page, signInPage, childDetails}) => {
            casename = 'CTSC different Child solicitors ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseWithMultipleChild);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await childDetails.gotoNextStep("Children");
            await childDetails.continueButton.click();
            await childDetails.addRegisteredSOlOrg();
            await childDetails.continueButton.click();
            await childDetails.assignDifferrentChildSolicitor();
            await childDetails.addDifferentSolicitorForChild('Child 1');
            await childDetails.addCafcassSolicitorForChild('Child 2');
            await childDetails.addCafcassSolicitorForChild('Child 3');
            await childDetails.addCafcassSolicitorForChild('Child 4');
            await childDetails.continueButton.click();
            await childDetails.checkYourAnsAndSubmit();
            await childDetails.tabNavigation('People in the case');
            await expect(page.getByText('Private solicitors', {exact: true})).toHaveCount(3);
            await expect(page.getByText('FPLSolicitorOrg', {exact: true})).toHaveCount(1);
            await childDetails.tabNavigation('Change of representatives')
            await expect(page.getByText('Added representative', {exact: true})).toHaveCount(4);
        });

    test('CTSC user able to add unregistered solicitor to a child ',
        async ({page, signInPage, childDetails}) => {
            casename = 'CTSC add unregistered child solicitor ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseWithMultipleChild);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await childDetails.gotoNextStep("Children");
            await childDetails.continueButton.click();
            await childDetails.addUnregisteredSolOrg();
            await childDetails.continueButton.click();
            await childDetails.assignSolicitorToAllChildren();
            await childDetails.continueButton.click();
            await childDetails.checkYourAnsAndSubmit();
            await childDetails.tabNavigation('People in the case');
            await expect(page.locator('#case-viewer-field-read--children1')).toContainText('Organisation (unregistered)');
            await expect(page.locator('#case-viewer-field-read--children1')).toContainText('NewOrganisation');
            await expect(page.getByRole('tab', {name: 'Change of representatives'})).toBeHidden();
        });

    test('CTSC user remove child solicitors',
        async ({page, signInPage, childDetails}) => {
            casename = 'CTSC change child solicitor ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseWithChildrenCafcassSolicitor);
            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[CHILDSOLICITORA]');
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await childDetails.gotoNextStep("Children");
            await childDetails.continueButton.click();
            await childDetails.removeSolicitor();
            await childDetails.continueButton.click();
            await childDetails.checkYourAnsAndSubmit();
            await childDetails.tabNavigation('People in the case');
            await expect(page.getByText('Private solicitors', {exact: true})).toHaveCount(0);
            await childDetails.tabNavigation('Change of representatives');
            await expect(page.getByText('Removed representative', {exact: true})).toHaveCount(1);
        });
});
