import {test} from '../fixtures/create-fixture';
import {
    CTSCTeamLeadUser,
    HearingCenterTeamLeader, judgeLondonUser,
} from '../settings/user-credentials';
import {expect} from '@playwright/test';


//The work type filter for some of the task names are skipped as the task creation test is not implemented for those task names. Once the task creation test is implemented for those task names, the work type filter can be added to the test cases.
test.describe('All works/ My works filter', () => {
    test.setTimeout(600_000);
    test.describe.configure({mode: 'serial'});
    const CTSCUsersWorkType = [
        {workType: 'Routine work', taskName: 'Review Correspondence'},
        {workType: 'Routine work', taskName: 'Review Response (CTSC)'},
        {workType: 'Routine work', taskName: 'Respond to a Query'},
        {workType: 'Applications', taskName: 'Review Standard Application'},
        {workType: 'Applications', taskName: 'Review Urgent Application'},
        {workType: 'Applications', taskName: 'Check Placement Application'},
        {workType: 'Decision-making work', taskName: 'Review Order (CTSC)'},
        {workType: 'Decision-making work', taskName: 'Review Standard Direction Order'},
        {workType: 'Error management', taskName: 'Review Failed Payment'},
    ]
    const JudicialUserWorkType = [
        //{  workType: 'Routine work', taskName: 'Review Message (Allocated Judge)' },
        // {workType: 'Routine work', taskName: 'Review Message (Hearing Judge)' }, Task creation test is not implemented for this task name, so skipping this test case
        //  { workType: 'Routine work', taskName: ' Review Message (Legal Adviser) ' },
        // { workType: 'Routine work', taskName: ' Review Message (Other Judiciary) ' },
        //    {  workType: 'Routine work', taskName: 'Review Response (Allocated Judge)' },
        // {workType: 'Routine work', taskName: 'Review Response (Hearing Judge)' }, Task creation test is not implemented for this task name, so skipping this test case
        //   { workType: 'Routine work', taskName: ' Review Response (Legal Adviser) ' },
        //    { workType: 'Routine work', taskName: ' Review Response (Other Judiciary) ' },
        {workType: 'Decision-making work', taskName: 'Approve Orders'},
        // { workType: 'Decision-making work', taskName: 'Approve Orders (Hearing Judge)' },
        //{ workType: 'Decision-making work', taskName: 'Approve Orders (Hearing Legal Adviser)' },
        {workType: 'Decision-making work', taskName: 'View Additional Applications'},
        // { workType: 'Decision-making work', taskName: 'View Additional Applications (Allocated Legal Adviser)' },
        //{ workType: 'Decision-making work', taskName: 'View Additional Applications (allocated Judge)' },

    ]
    const HearingCenterWorkType = [
        // {  workType: 'Routine work', taskName: 'Review Message (Local Court)' },
        // {workType: 'Routine work', taskName: 'Review Response (Local Court)' },
        {workType: 'Routine work', taskName: 'Review listing request'},
    ]
    const HighCourtAdminWorkType = [
        {workType: 'Routine work', taskName: 'Review Correspondence (High Court)'},
        {workType: 'Applications', taskName: 'Check Placement Application (High Court)'},
        {workType: 'Decision-making work', taskName: 'Review Standard Direction Order (High Court)'},
        {workType: 'Decision-making work', taskName: 'Review Order (High Court)'},
        {workType: 'Error management', taskName: 'Review Failed Payment (High Court)'},

    ]

    test('Judicial User All work search', async ({signInPage, allWork}) => {
        await signInPage.visit();
        await signInPage.login(judgeLondonUser.email, judgeLondonUser.password);
        await signInPage.page.pause();
        await signInPage.clickAllWork();

        for (const {workType, taskName} of JudicialUserWorkType) {
            await test.step(`${workType} - ${taskName}`, async () => {
                await allWork.resetFilters();
                await allWork.filterService('Family Public Law');
                await allWork.filterByWorkType(workType);
                await allWork.filterTaskBYRoleType('Judicial')
                await allWork.filterByTaskName(taskName);
                await allWork.clickSearchButton();
                console.log(`Searching for workType: ${workType}, taskName: ${taskName}`);
                await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText(taskName);

                await allWork.sortColumn('case_name', 'descending');
                await expect.soft(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText(taskName);
            });

        }
    });
    test.skip(' Hearing Center Team Leader All work search', async ({signInPage, allWork}) => {
        await signInPage.visit();
        await signInPage.login(HearingCenterTeamLeader.email, HearingCenterTeamLeader.password);
        await signInPage.page.pause();
        await signInPage.clickAllWork();

        for (const {workType, taskName} of HearingCenterWorkType) {
            await test.step(`${workType} - ${taskName}`, async () => {
                await allWork.resetFilters();
                await allWork.filterService('Family Public Law');
                await allWork.filterByWorkType(workType);
                await allWork.filterTaskBYRoleType('Admin')
                await allWork.filterByTaskName(taskName);
                await allWork.clickSearchButton();
                console.log(`Searching for workType: ${workType}, taskName: ${taskName}`);
                await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText(taskName);

                await allWork.sortColumn('case_name', 'descending');
                await expect.soft(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText(taskName);
            });

        }
    });
    test('CTSC User All work search', async ({signInPage, allWork}) => {
        await signInPage.visit();
        await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
        await signInPage.page.pause();
        await signInPage.clickAllWork();

        for (const {workType, taskName} of CTSCUsersWorkType) {
            await test.step(`${workType} - ${taskName}`, async () => {
                await allWork.resetFilters();
                await allWork.filterService('Family Public Law');
                await allWork.filterByWorkType(workType);
                await allWork.filterTaskBYRoleType('CTSC')
                await allWork.filterByTaskName(taskName);
                await allWork.clickSearchButton();
                console.log(`Searching for workType: ${workType}, taskName: ${taskName}`);
                await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText(taskName);

                await allWork.sortColumn('case_name', 'descending');
                await expect.soft(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText(taskName);
            });

        }
    });
    test('High Court User All work search', async ({signInPage, allWork}) => {
        await signInPage.visit();
        await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
        await signInPage.page.pause();
        await signInPage.clickAllWork();

        for (const {workType, taskName} of HighCourtAdminWorkType) {
            await test.step(`${workType} - ${taskName}`, async () => {
                await allWork.resetFilters();
                await allWork.filterService('Family Public Law');
                await allWork.filterByWorkType(workType);
                await allWork.filterTaskBYRoleType('Admin')
                await allWork.filterByTaskName(taskName);
                await allWork.clickSearchButton();
                console.log(`Searching for workType: ${workType}, taskName: ${taskName}`);
                await expect(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText(taskName);

                await allWork.sortColumn('case_name', 'descending');
                await expect.soft(allWork.taskTableFirstRow.locator('.cdk-column-task_title')).toContainText(taskName);
            });

        }
    });

});

