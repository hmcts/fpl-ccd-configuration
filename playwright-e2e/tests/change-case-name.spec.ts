import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: "json" };
import caseDataDemo from '../caseData/mandatorySubmissionFieldsDemo.json' assert {type: "json"};
import { CTSCTeamLeadUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser } from "../settings/user-credentials";
import { expect } from "@playwright/test";
import {urlConfig} from "../settings/urls";

test.describe('Change case name', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });
    test('Change case name',
        async ({ page, signInPage, changeCaseName,manageLaTransferToCourts }) => {
            caseName = 'CTSC Change case name ' + dateTime.slice(0, 10);
            if(urlConfig.env.toUpperCase() === 'DEMO'){
                await updateCase(caseName, caseNumber, caseDataDemo);
            }
            else{
                await updateCase(caseName, caseNumber, caseData);
            }
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email,CTSCTeamLeadUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await expect(changeCaseName.page.getByRole('heading', { name: 'CTSC Change case name' })).toBeVisible();

            await changeCaseName.gotoNextStep('Update case name');
            await changeCaseName.updateCaseName();
            await expect(changeCaseName.page.getByRole('heading', { name: 'Swansea City Council & Bloggs' })).toBeVisible();
        })
});
