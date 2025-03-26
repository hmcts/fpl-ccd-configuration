import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: "json" };
import caseDataDemo from '../caseData/mandatorySubmissionFieldsDemo.json' assert {type: "json"};
import caseDataWithTwoLA from '../caseData/mandatorySubmissionWithTwoLAFields.json' assert { type: "json" };
import caseDataWithTwoLADemo from'../caseData/mandatorySubmissionWithTwoLAFieldsDemo.json' assert {type: "json"};
import { CTSCTeamLeadUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser, CTSCUser } from "../settings/user-credentials";
import { expect } from "@playwright/test";
import {testConfig} from "../settings/test-config";
import {urlConfig} from "../settings/urls";

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
            await manageLaTransferToCourts.tabNavigation('Summary');
            //reload to fix the flakiness of summary details are not updated until reload
            await manageLaTransferToCourts.page.reload();
            await expect(page.getByText('Family Court sitting at Swansea')).toBeHidden();
            await expect(page.getByText('Family Court sitting at Central Family Court')).toBeVisible();

        })
    test('CTSC gives access to another local authority',
        async ({ page, signInPage, manageLaTransferToCourts }) => {
            caseName = 'CTSC gives access to another Local authority' + dateTime.slice(0, 10);
           if(urlConfig.env.toUpperCase() === 'DEMO'){
               await updateCase(caseName, caseNumber, caseDataDemo);
           }
           else{
            await updateCase(caseName, caseNumber, caseData);
           }
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');
            await manageLaTransferToCourts.updateCourtAccess();
            await manageLaTransferToCourts.tabNavigation('People in the case');
            await expect(page.getByText('Applicant 2')).toBeVisible();
            await expect(page.getByText('London Borough Hillingdon')).toBeVisible();

        })
    test('CTSC removes access',
        async ({ page, signInPage, manageLaTransferToCourts }) => {
            caseName = 'CTSC removed access' + dateTime.slice(0, 10);

            if(urlConfig.env.toUpperCase() === 'DEMO'){
                await updateCase(caseName, caseNumber, caseDataWithTwoLADemo);
            }
            else{
            await updateCase(caseName, caseNumber, caseDataWithTwoLA);
            }

            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');
            await manageLaTransferToCourts.updateRemoveAccess();
            await manageLaTransferToCourts.tabNavigation('People in the case');
            await expect(page.getByText('Applicant 2')).toBeHidden();
            await expect(page.getByText('London Borough Hillingdon')).toBeHidden();
        })
    test('CTSC tranfers to another local authority',
        async ({ page, signInPage, manageLaTransferToCourts }) => {
            caseName = 'CTSC transfers to another local authority' + dateTime.slice(0, 10);
            if(urlConfig.env.toUpperCase() === 'DEMO'){
                await updateCase(caseName, caseNumber, caseDataDemo);
            }
            else{
            await updateCase(caseName, caseNumber, caseData);
            }
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');
            await manageLaTransferToCourts.updateTranferToLa();
            await manageLaTransferToCourts.tabNavigation('People in the case');
            await expect(page.getByText('Swansea City Council')).toBeHidden();
            await expect(page.getByText('London Borough Hillingdon')).toBeVisible();
            await manageLaTransferToCourts.tabNavigation('Summary');
            await manageLaTransferToCourts.page.reload();
            await expect(page.getByText('Family Court sitting at Swansea')).toBeHidden();
            await expect(page.getByText('Family Court sitting at West London')).toBeVisible();
        })
});
