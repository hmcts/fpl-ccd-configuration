import {test} from '../fixtures/create-fixture';
//import {urlConfig} from "../settings/urls";
import caseData from "../caseData/mandatorySubmissionFields.json" assert {type: "json"};
import {CTSCTeamLeadUser, CTSCUser, newSwanseaLocalAuthorityUserOne} from '../settings/user-credentials';
import {expect} from '@playwright/test';
import {createCase, updateCase} from "../utils/api-helper";
import {describe} from "node:test";

//test is skipped until the issue with linked case retrieve  is resolved by EXUI team
test.describe.only('All works/ My works filter', () => {
    test.setTimeout(600_000);
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;
    let linkedCase1: string;
    let linkedCase2: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        expect(caseNumber).toBeDefined();
    });

    test('CTSC user search for  routine works- Review Correspondence',
        async ({page, signInPage, caseLink, allWork}) => {

            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.clickAllWork();
            // search for the routine CTSC work
            await allWork.filterService('Family Public Law');
            await allWork.filterWorkType('Routine work');
            await allWork.filterTaskName('Review Correspondence');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Correspondence');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Review Correspondence
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Correspondence');
    });
    test.only('CTSC user search for  routine works- Review Message CTSC',
        async ({page, signInPage, caseLink, allWork}) => {

            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.clickAllWork();
            // search for the routine CTSC work
            await allWork.filterService('Family Public Law');
            await allWork.filterWorkType('Routine work');
            await allWork.filterTaskName('Review Message (CTSC)');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Message (CTSC)');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Review Correspondence
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Message (CTSC)');
        });

});

