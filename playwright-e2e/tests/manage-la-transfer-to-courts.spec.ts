import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: "json" };
import caseDataDemo from '../caseData/mandatorySubmissionFieldsDemo.json' assert {type: "json"};
import caseDataWithTwoLA from '../caseData/mandatorySubmissionWithTwoLAFields.json' assert { type: "json" };
import caseDataWithTwoLADemo from'../caseData/mandatorySubmissionWithTwoLAFieldsDemo.json' assert {type: "json"};
import { CTSCTeamLeadUser, newSwanseaLocalAuthorityUserOne} from "../settings/user-credentials";
import { expect } from "@playwright/test";
import {urlConfig} from "../settings/urls";
import AxeBuilder from "@axe-core/playwright";

test.describe('Manage LAs / Transfer to court', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('CTSC transfer to a new court and submit case @xbrowser',
        async ({ page, signInPage, manageLaTransferToCourts }, testInfo) => {
        await test.step('Update Case Data', async() => {
            caseName = 'CTSC transfers case' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
        });

        await test.step('Team leader sign in and navigate to case details', async() => {
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
        });

        await test.step('LA update court case information', async() => {
            await manageLaTransferToCourts.gotoNextStep('Manage LAs / Transfer to court');
            await manageLaTransferToCourts.updateManageLaTransferToCourts();
            await manageLaTransferToCourts.tabNavigation('Summary');
        });

            await manageLaTransferToCourts.page.reload();

        await test.step('Check court information is Central Family Court', async() => {
            await expect(page.getByText('Family Court sitting at Swansea')).toBeHidden();
            await expect(page.getByText('Family Court sitting at Central Family Court')).toBeVisible();
        });

        await test.step('Run accessibility audit', async() => {

            const accessibilityScanResults = await new AxeBuilder({ page }).analyze();
            await testInfo.attach('accessibility-scan-results', {
                body: JSON.stringify(accessibilityScanResults, null, 2),
                contentType: 'application/json'
            });
        });
    });
    test('CTSC gives access to another local authority',
        async ({ page, signInPage, manageLaTransferToCourts }, testInfo) => {

            await test.step('Update Case Data', async () => {
                caseName = 'CTSC gives access to another Local authority' + dateTime.slice(0, 10);
                if (urlConfig.env.toUpperCase() === 'DEMO') {
                    await updateCase(caseName, caseNumber, caseDataDemo);
                } else {
                    await updateCase(caseName, caseNumber, caseData);
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
                await manageLaTransferToCourts.tabNavigation('People in the case');
            });

            await test.step('Verify new authority has access', async () => {
                await expect(page.getByText('Applicant 2')).toBeVisible();
                await expect(page.getByText('London Borough Hillingdon')).toBeVisible();
            });

            await test.step('Run accessibility audit', async() => {

                const accessibilityScanResults = await new AxeBuilder({ page }).analyze();
                await testInfo.attach('accessibility-scan-results', {
                    body: JSON.stringify(accessibilityScanResults, null, 2),
                    contentType: 'application/json'
                });
            });

        });
    test('CTSC removes access',
        async ({ page, signInPage, manageLaTransferToCourts }, testInfo) => {
            await test.step('Update Case Data', async () => {
                caseName = 'CTSC removed access' + dateTime.slice(0, 10);
                if (urlConfig.env.toUpperCase() === 'DEMO') {
                    await updateCase(caseName, caseNumber, caseDataWithTwoLADemo);
                } else {
                    await updateCase(caseName, caseNumber, caseDataWithTwoLA);
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
                await manageLaTransferToCourts.tabNavigation('People in the case');
            });

            await test.step('Verify access has been removed', async () => {
                await expect(page.getByText('Applicant 2')).toBeHidden();
                await expect(page.getByText('London Borough Hillingdon')).toBeHidden();
            });

            await test.step('Run accessibility audit', async() => {

                const accessibilityScanResults = await new AxeBuilder({ page }).analyze();
                await testInfo.attach('accessibility-scan-results', {
                    body: JSON.stringify(accessibilityScanResults, null, 2),
                    contentType: 'application/json'
                });
            });
        })
    test('CTSC tranfers to another local authority @xbrowser',
        async ({ page, signInPage, manageLaTransferToCourts }, testInfo) => {
            await test.step('Update Case Data', async () => {
                caseName = 'CTSC transfers to another local authority' + dateTime.slice(0, 10);
                if (urlConfig.env.toUpperCase() === 'DEMO') {
                    await updateCase(caseName, caseNumber, caseDataDemo);
                } else {
                    await updateCase(caseName, caseNumber, caseData);
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
                await manageLaTransferToCourts.tabNavigation('People in the case');
            });

            await test.step('Verify transfer of LA access and court update', async () => {
                await expect(page.getByText('Swansea City Council')).toBeHidden();
                await expect(page.getByText('London Borough Hillingdon')).toBeVisible();
                await manageLaTransferToCourts.tabNavigation('Summary');
                await manageLaTransferToCourts.page.reload();
                await expect(page.getByText('Family Court sitting at Swansea')).toBeHidden();
                await expect(page.getByText('Family Court sitting at West London')).toBeVisible();
            });

            await test.step('Run accessibility audit', async() => {

                const accessibilityScanResults = await new AxeBuilder({ page }).analyze();
                await testInfo.attach('accessibility-scan-results', {
                    body: JSON.stringify(accessibilityScanResults, null, 2),
                    contentType: 'application/json'
                });
            });
        })
});
