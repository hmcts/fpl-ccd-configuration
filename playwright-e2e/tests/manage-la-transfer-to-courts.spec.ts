import { test,expect } from '../fixtures/fixtures';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: "json" };
import caseDataDemo from '../caseData/mandatorySubmissionFieldsDemo.json' assert {type: "json"};
import caseDataWithTwoLA from '../caseData/mandatorySubmissionWithTwoLAFields.json' assert { type: "json" };
import caseDataWithTwoLADemo from'../caseData/mandatorySubmissionWithTwoLAFieldsDemo.json' assert {type: "json"};
import { CTSCTeamLeadUser, newSwanseaLocalAuthorityUserOne} from "../settings/user-credentials";
import { urlConfig } from "../settings/urls";
import { a11yHTMLReport } from "../utils/accessibility";
import { AxeResults } from 'axe-core';


test.describe('Manage LAs / Transfer to court', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    const axeResults: AxeResults[] = [];
    let axeScanReport: any;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        expect(caseNumber).toBeDefined();
    });

    test('CTSC transfer to a new court and submit case @xbrowser @testa11yReport',
        async ({ page, signInPage, manageLaTransferToCourts ,makeAxeBuilder}, testInfo) => {

            await test.step('Update Case Data', async() => {
                caseName = 'CTSC transfers case ' + dateTime.slice(0, 10);
                expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
            });

            await test.step('Sign in and navigate', async() => {
                await signInPage.visit();
                await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);
            });

            await test.step('Update court info', async() => {
                await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');

                await expect.soft(manageLaTransferToCourts.page.getByText('give case access to another local authority',{ exact: true })).toBeVisible();
                await expect.soft(manageLaTransferToCourts.page.getByText('remove case access from local authority',{ exact: true })).toBeVisible();
                await expect.soft(manageLaTransferToCourts.page.getByText('transfer a case to another local authority - they\'ll become the designated authority',{ exact: true })).toBeVisible();
                await expect.soft(manageLaTransferToCourts.page.getByText('transfer to another court',{ exact: true })).toBeVisible();
                //await manageLaTransferToCourts.page.pause();
                await manageLaTransferToCourts.selectLAAction('Transfer to another court');

                const a11yScanResultLAAction= await makeAxeBuilder().analyze();
                await a11yHTMLReport(a11yScanResultLAAction, testInfo, 'LA Action page A11y Report');

                await manageLaTransferToCourts.clickContinue();
                await manageLaTransferToCourts.selectCourt('Central Family Court');

                const a11yScanResultCourt = await makeAxeBuilder().analyze();
                await a11yHTMLReport(a11yScanResultCourt, testInfo, 'Court selection page A11y Report');

                await manageLaTransferToCourts.clickContinue();
                await manageLaTransferToCourts.checkYourAnsAndSubmit();

                await expect(manageLaTransferToCourts.page.getByText('has been updated with event: Manage LAs / Transfer to court')).toBeVisible();

            });

            await test.step('Validate court info', async() => {
                await manageLaTransferToCourts.tabNavigation('Summary');
                await manageLaTransferToCourts.page.reload();// to reflect the updated court info

                await expect(page.getByText('Family Court sitting at Swansea')).toBeHidden();
                await expect(page.getByText('Family Court sitting at Central Family Court')).toBeVisible();

                const a11yScanResultSummary = await makeAxeBuilder().analyze();
                await a11yHTMLReport(a11yScanResultSummary, testInfo, 'Summary Tab A11y Report');
            });
        });

    test('CTSC gives access to another local authority',
        async ({ page, signInPage, manageLaTransferToCourts,makeAxeBuilder }, testInfo) => {

            await test.step('Update Case Data', async () => {
                caseName = 'CTSC gives access to another Local authority' + dateTime.slice(0, 10);
                if (urlConfig.env.toUpperCase() === 'DEMO') {
                    expect(await updateCase(caseName, caseNumber, caseDataDemo)).toBeTruthy();
                } else {
                    expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
                }
            });


            await test.step('Team leader sign in and navigate to case details', async () => {
                await signInPage.visit();
                await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);
            });

            await test.step('CTSC gives access to another local authority', async () => {
                await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');
                await manageLaTransferToCourts.updateCourtAccess();

                const a11yScanResultLAAction= await makeAxeBuilder().analyze();
                await a11yHTMLReport(a11yScanResultLAAction, testInfo, 'Add New LA A11y Report');


                await manageLaTransferToCourts.clickContinue();
                await manageLaTransferToCourts.clickContinue();
                await manageLaTransferToCourts.checkYourAnsAndSubmit();
            });

            await test.step('Verify new authority has access', async () => {
                await manageLaTransferToCourts.tabNavigation('People in the case');
                await expect(page.getByText('Applicant 2')).toBeVisible();
                await expect(page.getByText('London Borough Hillingdon')).toBeVisible();
            });
        });

    test('CTSC removes access',
        async ({ page, signInPage, manageLaTransferToCourts ,makeAxeBuilder}, testInfo) => {
            await test.step('Update Case Data', async () => {
                caseName = 'CTSC removed access' + dateTime.slice(0, 10);
                if (urlConfig.env.toUpperCase() === 'DEMO') {
                     expect(await updateCase(caseName, caseNumber, caseDataWithTwoLADemo)).toBeTruthy();
                } else {
                    expect(await updateCase(caseName, caseNumber, caseDataWithTwoLA)).toBeTruthy();
                }
            });

            await test.step('Team leader sign in and navigate to case details', async () => {
                await signInPage.visit();
                await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);
            });

            await test.step('CTSC removes access', async () => {
                await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');
                await manageLaTransferToCourts.updateRemoveAccess();

                const a11yScanResultLAAction= await makeAxeBuilder().analyze();
                await a11yHTMLReport(a11yScanResultLAAction, testInfo, 'Remove LA access A11y Report');

                await manageLaTransferToCourts.clickContinue();
                await manageLaTransferToCourts.clickContinue();
                await manageLaTransferToCourts.checkYourAnsAndSubmit();

            });

            await test.step('Verify access has been removed', async () => {
                await manageLaTransferToCourts.tabNavigation('People in the case');
                await expect(page.getByText('Applicant 2')).toBeHidden();
                await expect(page.getByText('London Borough Hillingdon')).toBeHidden();
            });
        });

    test.only('CTSC tranfers to another local authority @xbrowser',
        async ({ page, signInPage, manageLaTransferToCourts,makeAxeBuilder }, testInfo) => {

        await test.step('Update Case Data', async () => {
                caseName = 'CTSC transfers to another local authority' + dateTime.slice(0, 10);
                if (urlConfig.env.toUpperCase() === 'DEMO') {
                    expect(await updateCase(caseName, caseNumber, caseDataDemo)).toBeTruthy();
                } else {
                   expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
                }
            });

            await test.step('Team leader sign in and navigate to case details', async () => {
                await signInPage.visit();
                await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);
            });

            await test.step('CTSC transfers case to another LA', async () => {
                await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');
                await manageLaTransferToCourts.updateTranferToLa();
                await manageLaTransferToCourts.clickContinue();
                await manageLaTransferToCourts.selectNewLocalAuthority('London Borough Hillingdon');
                const a11yScanResultNewLA= await makeAxeBuilder().analyze();
                await a11yHTMLReport(a11yScanResultNewLA, testInfo, 'select LA A11y Report');

                await manageLaTransferToCourts.clickContinue();
                await manageLaTransferToCourts.enterLAContactDetails();
                const a11yScanResultLAContact= await makeAxeBuilder().analyze();
                await a11yHTMLReport(a11yScanResultLAContact, testInfo, 'LA contact detail A11y Report');

                await manageLaTransferToCourts.clickContinue();
                await manageLaTransferToCourts.enterNewCourtDetails('Family Court sitting at West London');
                const a11yScanResultCourtDetails= await makeAxeBuilder().analyze();
                await a11yHTMLReport(a11yScanResultCourtDetails, testInfo, 'Court Details A11y Report');

                await manageLaTransferToCourts.clickContinue();
                await manageLaTransferToCourts.checkYourAnsAndSubmit();

            });

            await test.step('Verify transfer of LA access and court update', async () => {

                await manageLaTransferToCourts.tabNavigation('People in the case');

                await expect(page.getByText('Swansea City Council')).toBeHidden();
                await expect(page.getByText('London Borough Hillingdon')).toBeVisible();
                await manageLaTransferToCourts.tabNavigation('Summary');
                await manageLaTransferToCourts.page.reload();
                await expect(page.getByText('Family Court sitting at Swansea')).toBeHidden();
                await expect(page.getByText('Family Court sitting at West London')).toBeVisible();

            });
        });
});
