import {test} from '../fixtures/create-fixture';
//import {urlConfig} from "../settings/urls";
import caseData from "../caseData/mandatorySubmissionFields.json" assert {type: "json"};
import {
    CTSCTeamLeadUser,
    CTSCUser, judgeLondonUser,
    judgeWalesUser,
    newSwanseaLocalAuthorityUserOne
} from '../settings/user-credentials';
import {expect} from '@playwright/test';
import {createCase, updateCase} from "../utils/api-helper";
import {describe} from "node:test";

//test is skipped until the issue with linked case retrieve  is resolved by EXUI team
test.describe('All works/ My works filter', () => {
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
            await allWork.filterByWorkType('Routine work');
            await allWork.filterByTaskName('Review Correspondence');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Correspondence');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Review Correspondence
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Correspondence');
    });
    test('CTSC user search for  routine works- Review Message CTSC',
        async ({page, signInPage, caseLink, allWork}) => {

            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.clickAllWork();
            // search for the routine CTSC work
            await allWork.filterService('Family Public Law');
            await allWork.filterByWorkType('Routine work');
            await allWork.filterByTaskName('Review Response (CTSC)');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Response (CTSC)');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Review Response (CTSC)
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Response (CTSC)');
        });
    test('CTSC user search for  routine works- Respond to a Query',
        async ({page, signInPage, caseLink, allWork}) => {

            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.clickAllWork();
            // search for the routine CTSC work
            await allWork.filterService('Family Public Law');
            await allWork.filterByWorkType('Routine work');
            await allWork.filterByTaskName('Respond to a Query');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Respond to a Query');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Respond to a Query
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Respond to a Query');
        });
    test('CTSC user search for  application works- Review standard application',
        async ({page, signInPage, caseLink, allWork}) => {

            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.clickAllWork();
            // search for the routine CTSC work
            await allWork.filterService('Family Public Law');
            await allWork.filterByWorkType('Applications');
            await allWork.filterByTaskName('Review Standard Application');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Standard Application');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Review Standard Application
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Standard Application');
        });
    test('CTSC user search for  application works- Review urgent application',
        async ({page, signInPage, caseLink, allWork}) => {

            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.clickAllWork();
            // search for the routine CTSC work
            await allWork.filterService('Family Public Law');
            await allWork.filterByWorkType('Applications');
            await allWork.filterByTaskName('Review Urgent Application');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Urgent Application');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Review Urgent Application
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Urgent Application');
        });
    test('CTSC user search for  application works- Check Placement Application',
        async ({page, signInPage, caseLink, allWork}) => {

            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.clickAllWork();
            // search for the routine CTSC work
            await allWork.filterService('Family Public Law');
            await allWork.filterByWorkType('Applications');
            await allWork.filterByTaskName('Check Placement Application');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Check Placement Application');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Check Placement Application
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Check Placement Application');
        });
    test('CTSC user search for  Decision-making work works- Review Order (CTSC)',
        async ({page, signInPage, caseLink, allWork}) => {

            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.clickAllWork();
            // search for the routine CTSC work
            await allWork.filterService('Family Public Law');
            await allWork.filterByWorkType('Decision-making work');
            await allWork.filterByTaskName('Review Order (CTSC)');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Order (CTSC)');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Review Order (CTSC)
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Order (CTSC)');
        });
    test('CTSC user search for  Decision-making works- Review Standard Direction Order ',
        async ({page, signInPage, caseLink, allWork}) => {

            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.clickAllWork();
            // search for the routine CTSC work
            await allWork.filterService('Family Public Law');
            await allWork.filterByWorkType('Decision-making work');
            await allWork.filterByTaskName('Review Standard Direction Order');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Standard Direction Order');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Review Standard Direction Order
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Standard Direction Order');
        });
    test('CTSC user search for Error management work - Review Failed Payment ',
        async ({page, signInPage, caseLink, allWork}) => {

            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.clickAllWork();
            // search for the routine CTSC work
            await allWork.filterService('Family Public Law');
            await allWork.filterByWorkType('Error management');
            await allWork.filterByTaskName('Review Failed Payment');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Failed Payment');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Review Failed Payment
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Review Failed Payment');
        });

    test('Legal user search for Decision-making work - Approve Orders ',
        async ({page, signInPage, caseLink, allWork}) => {

            await signInPage.visit();
            await signInPage.login(judgeLondonUser.email, judgeLondonUser.password);
            await signInPage.clickAllWork();
            // search for the routine CTSC work
            await allWork.filterService('Family Public Law');
            await allWork.filterByWorkType('Decision-making work');
            await allWork.filterTaskBYRoleType('Judicial') // Replace 'SomeRoleType' with the actual role type
            await allWork.filterByTaskName('Approve Orders');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Approve Orders');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Approve Orders
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Approve Orders');
        });
    test('Legal user search for Decision-making work - Review Failed Payment ',
        async ({page, signInPage, caseLink, allWork}) => {

            await signInPage.visit();
            await signInPage.login(judgeLondonUser.email, judgeLondonUser.password);
            await signInPage.clickAllWork();
            // search for the routine CTSC work
            await allWork.filterService('Family Public Law');
            await allWork.filterByWorkType('Decision-making work');
            await allWork.filterTaskBYRoleType('Judicial') // Replace 'SomeRoleType' with the actual role type
            await allWork.filterByTaskName('Approve Orders');
            await allWork.clickSearchButton();
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Approve Orders');
            // assert the row one have review correspondence task name
            await allWork.sortColumn('case_name','descending');
            // assert the first row have the task name as Approve Orders
            await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText('Approve Orders');
        });
});

