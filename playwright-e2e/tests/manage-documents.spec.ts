import { test } from '../fixtures/create-fixture';
import { testConfig } from '../settings/test-config';
import { CTSCUser, newSwanseaLocalAuthorityUserOne, privateSolicitorOrgUser } from '../settings/user-credentials';
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: 'json' };
import caseWithResSolicitor from '../caseData/caseWithRespondentSolicitor.json' assert { type: 'json' };
import { expect } from '@playwright/test'
import {createCase, giveAccessToCase, updateCase} from "../utils/api-helper";

test.describe('Manage Documents', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('LA uploads correspondence documents', async ({ page, signInPage, manageDocuments, caseFileView }) => {
        caseName = 'LA uploads correspondence documents ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await manageDocuments.uploadDocuments('Court correspondence');

        // Check CFV
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Court Correspondence');
        await expect(page.getByRole('tree')).toContainText('testTextFile.txt');

        // If WA is enabled
        if (testConfig.waEnabled) {
            console.log('WA testing');
            await manageDocuments.clickSignOut();
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);

            await signInPage.navigateTOCaseDetails(caseNumber);

            // Judge in Wales should see this Welsh case task + be able to assign it to themselves
            await manageDocuments.tabNavigation('Tasks');
            await manageDocuments.waitForTask('Review Correspondence');

            // Assign and complete the task
            await page.getByText('Assign to me').click();
            await page.getByText('Mark as done').click();
            await page.getByRole('button', { name: "Mark as done" }).click();

            // Should be no more tasks on the page
            await expect(page.getByText('Review Correspondence')).toHaveCount(0);
        }
    });
    test('LA uploads various documents visble in CFV', async ({ signInPage, manageDocuments }) => {
        caseName = 'LA uploads various documents ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseWithResSolicitor);
        await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]');
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await manageDocuments.gotoNextStep('Manage documents');
        await manageDocuments.uploadDocuments('Position Statements');
    });
    test('LA uploads confidential documents visible in CFV ', async ({ signInPage, manageDocuments, caseFileView }) => {
        caseName = 'LA uploads various documents ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await manageDocuments.gotoNextStep('Manage documents');
        await manageDocuments.uploadConfidentialDocuments('Position Statements');
    });
    test('HMCTS uploads confidential documents visible in CFV ', async ({ signInPage, manageDocuments }) => {
        caseName = 'HMCTS uploads variuos documents ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseWithResSolicitor);
        await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]');
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await manageDocuments.gotoNextStep('Manage documents');
        await manageDocuments.uploadConfidentialDocuments('Position Statements');
        await manageDocuments.clickSignOut();
        await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
    });
    test('LA uploads correspodence documents visible in correct folder ', async ({ signInPage, manageDocuments, caseFileView }) => {
        caseName = 'LA uploads correspondence documents visible in correct order ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await manageDocuments.gotoNextStep('Manage documents');
        await manageDocuments.uploadConfidentialDocuments('Position Statements');
        await manageDocuments.clickSignOut();
        await signInPage.login('solicitor1@solicitors.uk', 'Password12');
        await signInPage.login(CTSCUser.email, CTSCUser.password);
    });
    test('LA removes document ', async ({ page, signInPage, manageDocuments }) => {
        caseName = 'LA removes document ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await manageDocuments.gotoNextStep('Removes documents');
        await page.getByLabel('Uploaded Document').selectOption('1: hearingDocuments.posStmtList###3ad0ca08-1c4c-48');
        await page.getByLabel('There is a mistake on the').check();
    });
    test('CTSC user can move document between folder ', async ({ page, signInPage, manageDocuments }) => {
        caseName = 'CTSC moved documents between folder ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await page.getByText('Case File View').click();
        await page.getByRole('button', { name: 'toggle Position Statements' }).click();
        await page.getByRole('button', { name: 'More document options', exact: true }).click();
        await page.getByText('Change folder').click();
        await page.getByLabel('Threshold', { exact: true }).check();
    });
});
