import { test } from '../fixtures/create-fixture';
import { testConfig } from '../settings/test-config';
import { createRequire } from 'node:module';
const require = createRequire(import.meta.url);
import { CTSCUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser, privateSolicitorOrgUser } from '../settings/user-credentials';
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: 'json' };
import { expect } from '@playwright/test';
import { createCase, giveAccessToCase, updateCase } from "../utils/api-helper";
import { setHighCourt } from '../utils/update-case-details';


test.describe('Manage Documents', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    const caseData = require('../caseData/mandatorySubmissionFields.json');
    const caseWithResSolicitor = require('../caseData/caseWithRespondentSolicitor.json');
    const caseWithManageDocumentUploads = require('../caseData/caseWithManageDocumentUploads.json');
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('LA uploads documents', async ({ page, signInPage, manageDocuments, caseFileView }) => {
        caseName = 'LA uploads documents ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await manageDocuments.gotoNextStep('Manage documents');
        await manageDocuments.uploadDocuments('Court correspondence');

        // Check CFV
        await signInPage.navigateTOCaseDetails(caseNumber);
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


    test('LA uploads Position Statements visible in CFV', async ({ signInPage, manageDocuments, caseFileView, page }) => {
        caseName = 'LA uploads Position Statements visible in CFV ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseWithResSolicitor);
        await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]');
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await manageDocuments.gotoNextStep('Manage documents');
        await manageDocuments.uploadDocuments('Position Statements');

        // uploads documents
       // await manageDocuments.uploadNewDocuments();

        // position is visble under CFV
        await signInPage.navigateTOCaseDetails(caseNumber);
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Position Statements');
        await expect(page.getByRole('tree')).toContainText(' testTextFile.txt ');
        await signInPage.logout();

        //Login as respondence solicitor
        await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
        await signInPage.isSignedIn();
        await signInPage.navigateTOCaseDetails(caseNumber);

        //go to CFV and assert Position statement  visible
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Position Statements');
        await expect(page.getByRole('tree')).toContainText('testTextFile.txt');
    });

    test('LA uploads confidential documents visible in CFV not visible to solicitor', async ({ signInPage, manageDocuments, caseFileView, page }) => {
        caseName = 'LA uploads confidential position document ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]');
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await manageDocuments.gotoNextStep('Manage documents');
        await manageDocuments.uploadConfidentialDocuments('Position Statements');

        // position is visble under CFV
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Position Statements');
        await caseFileView.openFolder('Confidential');
        await expect(page.getByRole('tree')).toContainText('testTextFile.txt');

        //Login as respondence solicitor
        await signInPage.logout();
        await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
        await signInPage.isSignedIn();
        await signInPage.navigateTOCaseDetails(caseNumber);

        //go to CFV and assert Position statement not visble
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Position Statements');
        await expect(page.getByRole('tree')).not.toContainText('testTextFile.txt');
        await caseFileView.openFolder('Confidential');
        await expect(page.getByRole('tree')).not.toContainText('testTextFile.txt');
    });

    test('CTSC uploads confidential documents visible in CFV not visible to solicitor ', async ({ signInPage, manageDocuments, caseFileView, page }) => {
        caseName = 'CTSC uploads confidential Position statement documents  ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseWithResSolicitor);
        await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]');
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await manageDocuments.gotoNextStep('Manage documents');
        await manageDocuments.uploadConfidentialDocuments('Position Statements');

        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Position Statements');
        await caseFileView.openFolder('Confidential');
        await expect(page.getByRole('tree')).toContainText('testTextFile.txt');
        await signInPage.logout();

        //Login as respondence solicitor
        await signInPage.visit();
        await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
        await signInPage.isSignedIn();
        await signInPage.navigateTOCaseDetails(caseNumber);

        //go to CFV and assert Position statement not visble
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Position Statements');
        await expect(page.getByRole('tree')).not.toContainText('testTextFile.txt');
        await caseFileView.openFolder('Confidential');
        await expect(page.getByRole('tree')).not.toContainText('testTextFile.txt');
        await signInPage.logout();

        //login in LA and assert position statement not visible

        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.isSignedIn();
        await signInPage.navigateTOCaseDetails(caseNumber);

        //go to CFV and assert Position statement not visble
        await signInPage.navigateTOCaseDetails(caseNumber);
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Position Statements');
        await expect(page.getByRole('tree')).not.toContainText('testTextFile.txt');
        await caseFileView.openFolder('Confidential');
        await expect(page.getByRole('tree')).not.toContainText('testTextFile.txt');


    });

    test('CTSC removes document ', async ({ page, signInPage, manageDocuments, caseFileView }) => {
        caseName = 'CTSC removes document ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseWithManageDocumentUploads);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);

        // remove documents
        await manageDocuments.gotoNextStep('Manage documents');
        await manageDocuments.removeDocuments();

        //go to CFV and assert Court Correspondence not visble
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Court Correspondence');
        await expect(page.getByRole('tree')).not.toContainText('mock.pdf');
    });

    test('CTSC user can move document between folder ', async ({ page, signInPage, caseFileView }) => {
        caseName = 'CTSC moved documents between folder ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseWithManageDocumentUploads);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await caseFileView.goToCFVTab();
        await caseFileView.moveDocument('Court Correspondence', 'Threshold');
        await caseFileView.openFolder('Threshold');
        await expect(page.getByRole('tree')).toContainText('mock.pdf');

    });

    test('High Court Review Correspondence WA task', async ({ page, signInPage, manageDocuments, caseFileView }) => {
    caseName = 'High Court Review Correspondence WA task ' + dateTime.slice(0, 10);
    setHighCourt(caseData);
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
      await signInPage.login(HighCourtAdminUser.email, HighCourtAdminUser.password);

      await signInPage.navigateTOCaseDetails(caseNumber);

      await manageDocuments.tabNavigation('Tasks');
      await manageDocuments.waitForTask('Review Correspondence (High Court)');

      // Assign and complete the task
      await page.getByText('Assign to me').click();
      await page.getByText('Mark as done').click();
      await page.getByRole('button', { name: "Mark as done" }).click();

      // Should be no more tasks on the page
      await expect(page.getByText('Review Correspondence (High Court)')).toHaveCount(0);
    }
  });

});
