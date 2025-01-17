import { test } from '../fixtures/create-fixture';
import { newSwanseaLocalAuthorityUserOne } from '../settings/user-credentials';
import { expect } from "@playwright/test";
import { testConfig } from '../settings/test-config';
import caseData from '../caseData/mandatorySubmissionFieldsWithoutAdditionalApp.json' assert { type: "json" };
import { setHighCourt } from '../utils/update-case-details';
import { createCase, updateCase } from "../utils/api-helper";

//Mark the test serial as document upload are time rated for a user by EXUI
test.describe.configure({ mode: 'serial' });

test.describe('Upload additional applications', () => {
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let caseName: string;

    test.beforeEach(async () => {
    caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });
    //mark test as slow to give extra timeout
    test.slow();



  test('LA uploads a C1 application',
    async ({ localAuthorityUser,legalUser, additionalApplications }) => {
      caseName = 'LA uploads an other application ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await additionalApplications.switchUser(localAuthorityUser.page);
      await additionalApplications.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseOtherApplicationType();
      await additionalApplications.fillOtherApplicationDetails();

      // Payment details
      await expect(additionalApplications.page.getByText('£255.00')).toBeVisible();
      await additionalApplications.payForApplication();

      await additionalApplications.checkYourAnsAndSubmit();

      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(additionalApplications.page.getByText('PBA1234567')).toBeVisible();
      await expect(additionalApplications.page.getByText('C1 - Change surname or remove from jurisdiction')).toBeVisible();
      await expect(additionalApplications.page.getByText('On the same day')).toBeVisible();

      // If WA is enabled
      if (testConfig.waEnabled) {
        console.log('WA testing');
        await additionalApplications.switchUser(legalUser.page);
        await additionalApplications.navigateTOCaseDetails(caseNumber);

        // Judge in Wales should see this Welsh case task + be able to assign it to themselves
        await additionalApplications.tabNavigation('Tasks');
        await additionalApplications.waitForTask('View Additional Applications');

        // Assign and complete the task
        await additionalApplications.page.getByText('Assign to me').click();
        await additionalApplications.page.getByText('Mark as done').click();
        await additionalApplications.page.getByRole('button', {name: "Mark as done"}).click();

        // Should be no more tasks on the page
        await expect(additionalApplications.page.getByText('View Additional Applications')).toHaveCount(0);
      }
    });

  test('LA uploads a C2 application with draft order',
    async ({ localAuthorityUser, additionalApplications }) => {
      caseName = 'LA uploads a C2 application with draft order ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await additionalApplications.switchUser(localAuthorityUser.page)
      await additionalApplications.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseC2ApplicationType();
      await additionalApplications.fillC2ApplicationDetails();

      // Payment details
      await expect(additionalApplications.page.getByText('£255.00')).toBeVisible();
      await additionalApplications.payForApplication();

      await additionalApplications.checkYourAnsAndSubmit();
  });

  test('LA uploads combined Other and C2 applications',
    async ({localAuthorityUser, additionalApplications }) => {
      caseName = 'LA uploads additional application with both Other and C2 ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await additionalApplications.switchUser(localAuthorityUser.page)
      await additionalApplications.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseBothApplicationTypes();
      await additionalApplications.fillC2ApplicationDetails();
      await additionalApplications.fillOtherApplicationDetails();

      await expect(localAuthorityUser.page.getByText('£255.00')).toBeVisible();
      await additionalApplications.payForApplication();

      await additionalApplications.checkYourAnsAndSubmit();

      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(localAuthorityUser.page.getByText('PBA1234567')).toBeVisible();
      await expect(localAuthorityUser.page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
      await expect(localAuthorityUser.page.getByText('On the same day')).toBeVisible(); // Other application
      await expect(localAuthorityUser.page.getByText('Within 2 days')).toBeVisible(); // C2 application

      // can see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(localAuthorityUser.page.getByText('Draft order title')).toBeVisible();
    });

    test('LA uploads a confidential C2 application with draft order',
        async ({ localAuthorityUser,ctscUser, additionalApplications }) => {
            caseName = 'LA uploads a confidential C2 application with draft order ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await additionalApplications.switchUser(localAuthorityUser.page)
            await additionalApplications.navigateTOCaseDetails(caseNumber);
            await additionalApplications.gotoNextStep('Upload additional applications');
            await additionalApplications.chooseConfidentialC2ApplicationType();
            await additionalApplications.fillC2ApplicationDetails();

            // Payment details
            await expect(localAuthorityUser.page.getByText('£255.00')).toBeVisible();
            await additionalApplications.payForApplication();

            await additionalApplications.checkYourAnsAndSubmit();

            await additionalApplications.tabNavigation('Other applications');

            // can see some basic properties of the application
            await expect(localAuthorityUser.page.getByText('PBA1234567')).toBeVisible();
            await expect(localAuthorityUser.page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
            await expect(localAuthorityUser.page.getByText('Within 2 days')).toBeVisible();

            // can see the draft order to be approved
            await additionalApplications.tabNavigation('Draft orders');
            await expect(localAuthorityUser.page.getByText('Draft order title')).toBeVisible();


            await additionalApplications.switchUser(ctscUser.page)
            await additionalApplications.navigateTOCaseDetails(caseNumber);

            // CTSC can see some basic properties of the application
            await additionalApplications.tabNavigation('Other applications');
            await expect(additionalApplications.page.getByText('PBA1234567')).toBeVisible();
            await expect(additionalApplications.page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
            await expect(additionalApplications.page.getByText('Within 2 days')).toBeVisible();

            // CTSC can see the draft order to be approved
            await additionalApplications.tabNavigation('Draft orders');
            await expect(additionalApplications.page.getByText('Draft order title')).toBeVisible();
        });

    test('CTSC uploads a confidential C2 application with draft order',
        async ({ ctscUser, localAuthorityUser,additionalApplications }) => {
            caseName = 'CTSC uploads a confidential C2 application with draft order ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await additionalApplications.switchUser(ctscUser.page);

            await additionalApplications.navigateTOCaseDetails(caseNumber);

            await additionalApplications.gotoNextStep('Upload additional applications');
            await additionalApplications.chooseConfidentialC2ApplicationType();
            await additionalApplications.fillC2ApplicationDetails();

            // Payment details
            await expect(ctscUser.page.getByText('£255.00')).toBeVisible();
            await additionalApplications.payForApplication();

            await additionalApplications.checkYourAnsAndSubmit();

            await additionalApplications.tabNavigation('Other applications');

            // can see some basic properties of the application
            await expect(additionalApplications.page.getByText('PBA1234567')).toBeVisible();
            await expect(additionalApplications.page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
            await expect(additionalApplications.page.getByText('Within 2 days')).toBeVisible();

            // can see the draft order to be approved
            await additionalApplications.tabNavigation('Draft orders');
            await expect(additionalApplications.page.getByText('Draft order title')).toBeVisible();

            await additionalApplications.switchUser(localAuthorityUser.page);
            await additionalApplications.navigateTOCaseDetails(caseNumber);

            // LA cannot see some basic properties of the application
            await additionalApplications.tabNavigation('Other applications');
            await expect(additionalApplications.page.getByText('This is a confidential application and restricted viewing applies')).toBeVisible();

            // LA cannot see the draft order to be approved
            await additionalApplications.tabNavigation('Draft orders');
            await expect(additionalApplications.page.getByText('This is a confidential draft order and restricted viewing applies')).toBeVisible();
        });

      test('Failed Payment High Court WA task', async ({ localAuthorityUser,courtAdminUser,additionalApplications, caseFileView }) => {
        caseName = 'Failed Payment High Court WA task ' + dateTime.slice(0, 10);
        setHighCourt(caseData);
        await updateCase(caseName, caseNumber, caseData);
        await additionalApplications.switchUser(localAuthorityUser.page);
        await additionalApplications.navigateTOCaseDetails(caseNumber);
        await additionalApplications.uploadBasicC2Application(false);

        // Check CFV
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Application');
        await caseFileView.openFolder('C2 applications');
        await expect(localAuthorityUser.page.getByRole('tree')).toContainText('testTextFile.pdf');

        // If WA is enabled
        if (testConfig.waEnabled) {
            await additionalApplications.switchUser(courtAdminUser.page)
            await additionalApplications.navigateTOCaseDetails(caseNumber);

            // Judge in Wales should see this Welsh case task + be able to assign it to themselves
            await additionalApplications.tabNavigation('Tasks');
            await additionalApplications.waitForTask('Failed Payment (High Court)');

            // Assign and complete the task
            await additionalApplications.page.getByText('Assign to me').click();
            await additionalApplications.page.getByText('Mark as done').click();
            await additionalApplications.page.getByRole('button', { name: "Mark as done" }).click();

            // Should be no more tasks on the page
            await expect(additionalApplications.page.getByText('Failed Payment (High Court)')).toHaveCount(0);
        }
    });
});
