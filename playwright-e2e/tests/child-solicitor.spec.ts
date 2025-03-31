import { test } from '../fixtures/create-fixture';
import {createCase, giveAccessToCase, updateCase} from "../utils/api-helper";
import caseWithChildrenCafcassSolicitor from '../caseData/caseWithMultipleChildCafcassSolicitor.json' assert { type: "json" }
import caseWithMultipleChild from '../caseData/mandatorySubmissionFields.json' assert { type: "json" }
import caseWithChildrenCafcassSolicitorDemo from '../caseData/caseWithMultipleChildCafcassSolicitorDemo.json' assert{ type: "json"}
import {
    newSwanseaLocalAuthorityUserOne,
    privateSolicitorOrgUser,
    FPLSolicitorOrgUser,
    CTSCTeamLeadUser
} from '../settings/user-credentials';
import { expect } from '@playwright/test';
import {urlConfig} from "../settings/urls";

test.describe('Manage child representatives ', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('CTSC user can add one legal representative to all children ',
        async ({ctscUser, childDetails}) => {
            casename = 'CTSC add one solicitor to represent all children ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseWithMultipleChild);
            await childDetails.switchUser(ctscUser.page);
            // await signInPage.visit();
            // await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await childDetails.navigateTOCaseDetails(caseNumber);
            await childDetails.gotoNextStep("Children");
            await childDetails.clickContinue();
            await childDetails.addRegisteredSOlOrg();
            await childDetails.clickContinue();
            await childDetails.assignSolicitorToAllChildren();
            await childDetails.clickContinue();
            await childDetails.checkYourAnsAndSubmit();
            await childDetails.tabNavigation('People in the case');
            await expect(childDetails.page.getByText('Private solicitors', {exact: true})).toHaveCount(4);
            await childDetails.tabNavigation('Change of representatives')
            await expect(childDetails.page.getByText('Added representative', {exact: true})).toHaveCount(4);
        });

    test(' CTSC user can add different legal representative to each children',
        async ({ctscUser, childDetails}) => {
            casename = 'CTSC different Child solicitors ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseWithMultipleChild);
            // await signInPage.visit();
            // await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await childDetails.switchUser(ctscUser.page);
            await childDetails.navigateTOCaseDetails(caseNumber);
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
            await expect(childDetails.page.getByText('Private solicitors', {exact: true})).toHaveCount(3);
            await expect(childDetails.page.getByText('FPLSolicitorOrg', {exact: true})).toHaveCount(1);
            await childDetails.tabNavigation('Change of representatives')
            await expect(childDetails.page.getByText('Added representative', {exact: true})).toHaveCount(4);
        });

    test('CTSC user able to add unregistered solicitor to a child ',
        async ({ctscUser, childDetails}) => {
            casename = 'CTSC add unregistered child solicitor ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseWithMultipleChild);
            // await signInPage.visit();
            // await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await childDetails.switchUser(ctscUser.page);
            await childDetails.navigateTOCaseDetails(caseNumber);
            await childDetails.gotoNextStep("Children");
            await childDetails.clickContinue();
            await childDetails.addUnregisteredSolOrg();
            await childDetails.clickContinue();
            await childDetails.assignSolicitorToAllChildren();
            await childDetails.clickContinue();
            await childDetails.checkYourAnsAndSubmit();
            await childDetails.tabNavigation('People in the case');
            await expect(childDetails.page.locator('#case-viewer-field-read--children1')).toContainText('Organisation (unregistered)');
            await expect(childDetails.page.locator('#case-viewer-field-read--children1')).toContainText('NewOrganisation');
            await expect(childDetails.page.getByRole('tab', {name: 'Change of representatives'})).toBeHidden();
        });

    test('CTSC user remove child solicitors',
        async ({ctscUser, childDetails}) => {
            casename = 'CTSC change child solicitor ' + dateTime.slice(0, 10);
            if(urlConfig.env=='demo') {
                await updateCase(casename, caseNumber, caseWithChildrenCafcassSolicitorDemo);
            }
            else{
                await updateCase(casename, caseNumber, caseWithChildrenCafcassSolicitor);
            }
            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[CHILDSOLICITORA]');
            // await signInPage.visit();
            // await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            childDetails.switchUser(ctscUser.page);
            await childDetails.navigateTOCaseDetails(caseNumber);
            await childDetails.gotoNextStep("Children");
            await childDetails.clickContinue();
            await childDetails.removeSolicitor();
            await childDetails.clickContinue();
            await childDetails.checkYourAnsAndSubmit();
            await childDetails.tabNavigation('People in the case');
            await expect(childDetails.page.getByText('Private solicitors', {exact: true})).toHaveCount(0);
            await childDetails.tabNavigation('Change of representatives');
            await expect(childDetails.page.getByText('Removed representative', {exact: true})).toHaveCount(1);
        });
});
