import { test } from '../fixtures/create-fixture';
import { createRequire } from 'node:module';
const require = createRequire(import.meta.url);
import { newSwanseaLocalAuthorityUserOne, judgeWalesUser, CTSCUser } from '../settings/user-credentials';
import { expect } from "@playwright/test";
import { testConfig } from '../settings/test-config';
import {createCase, updateCase} from "../utils/api-helper";

test.describe(' Upload additional applications', () => {
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let caseName: string;
  const caseData = require('../caseData/mandatorySubmissionFieldsWithoutAdditionalApp.json');



    test.beforeEach(async () => {
    caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });

  test('@local LA uploads a C1 application',
    async ({ page, signInPage, additionalApplications }) => {
      caseName = 'LA uploads an other application ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseOtherApplicationType();
      await additionalApplications.fillOtherApplicationDetails();

      // Payment details
      await expect(page.getByText('£255.00')).toBeVisible();
      await additionalApplications.payForApplication();

      await additionalApplications.checkYourAnsAndSubmit();

      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText('PBA1234567')).toBeVisible();
      await expect(page.getByText('C1 - Change surname or remove from jurisdiction')).toBeVisible();
      await expect(page.getByText('On the same day')).toBeVisible();

      // If WA is enabled
      if (testConfig.waEnabled) {
        console.log('WA testing');
        await additionalApplications.clickSignOut();
        await signInPage.visit();
        await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);

        // Judge in Wales should see this Welsh case task + be able to assign it to themselves
        await additionalApplications.tabNavigation('Tasks');
        await additionalApplications.waitForTask('View Additional Applications');

        // Assign and complete the task
        await page.getByText('Assign to me').click();
        await page.getByText('Mark as done').click();
        await page.getByRole('button', {name: "Mark as done"}).click();

        // Should be no more tasks on the page
        await expect(page.getByText('View Additional Applications')).toHaveCount(0);
      }
    });

  test('LA uploads a C2 application with draft order',
    async ({ page, signInPage, additionalApplications }) => {
      caseName = 'LA uploads a C2 application with draft order ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseC2ApplicationType();
      await additionalApplications.fillC2ApplicationDetails();

      // Payment details
      await expect(page.getByText('£255.00')).toBeVisible();
      await additionalApplications.payForApplication();

      await additionalApplications.checkYourAnsAndSubmit();

      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText('PBA1234567')).toBeVisible();
      await expect(page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
      await expect(page.getByText('Within 2 days')).toBeVisible();

      // can see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('Draft order title')).toBeVisible();
    });

  test('LA uploads combined Other and C2 applications',
    async ({ page, signInPage, additionalApplications }) => {
      caseName = 'LA uploads additional application with both Other and C2 ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseBothApplicationTypes();
      await additionalApplications.fillC2ApplicationDetails();
      await additionalApplications.fillOtherApplicationDetails();

      await expect(page.getByText('£255.00')).toBeVisible();
      await additionalApplications.payForApplication();

      await additionalApplications.checkYourAnsAndSubmit();

      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText('PBA1234567')).toBeVisible();
      await expect(page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
      await expect(page.getByText('On the same day')).toBeVisible(); // Other application
      await expect(page.getByText('Within 2 days')).toBeVisible(); // C2 application

      // can see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('Draft order title')).toBeVisible();
    });

    test('LA uploads a confidential C2 application with draft order',
        async ({ page, signInPage, additionalApplications }) => {
            caseName = 'LA uploads a confidential C2 application with draft order ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await additionalApplications.gotoNextStep('Upload additional applications');
            await additionalApplications.chooseConfidentialC2ApplicationType();
            await additionalApplications.fillC2ApplicationDetails();

            // Payment details
            await expect(page.getByText('£255.00')).toBeVisible();
            await additionalApplications.payForApplication();

            await additionalApplications.checkYourAnsAndSubmit();

            await additionalApplications.tabNavigation('Other applications');

            // can see some basic properties of the application
            await expect(page.getByText('PBA1234567')).toBeVisible();
            await expect(page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
            await expect(page.getByText('Within 2 days')).toBeVisible();

            // can see the draft order to be approved
            await additionalApplications.tabNavigation('Draft orders');
            await expect(page.getByText('Draft order title')).toBeVisible();

            await additionalApplications.clickSignOut();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            // CTSC can see some basic properties of the application
            await additionalApplications.tabNavigation('Other applications');
            await expect(page.getByText('PBA1234567')).toBeVisible();
            await expect(page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
            await expect(page.getByText('Within 2 days')).toBeVisible();

            // CTSC can see the draft order to be approved
            await additionalApplications.tabNavigation('Draft orders');
            await expect(page.getByText('Draft order title')).toBeVisible();
        });

    test('CTSC uploads a confidential C2 application with draft order',
        async ({ page, signInPage, additionalApplications }) => {
            caseName = 'CTSC uploads a confidential C2 application with draft order ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await additionalApplications.gotoNextStep('Upload additional applications');
            await additionalApplications.chooseConfidentialC2ApplicationType();
            await additionalApplications.fillC2ApplicationDetails();

            // Payment details
            await expect(page.getByText('£255.00')).toBeVisible();
            await additionalApplications.payForApplication();

            await additionalApplications.checkYourAnsAndSubmit();

            await additionalApplications.tabNavigation('Other applications');

            // can see some basic properties of the application
            await expect(page.getByText('PBA1234567')).toBeVisible();
            await expect(page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
            await expect(page.getByText('Within 2 days')).toBeVisible();

            // can see the draft order to be approved
            await additionalApplications.tabNavigation('Draft orders');
            await expect(page.getByText('Draft order title')).toBeVisible();

            await additionalApplications.clickSignOut();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            // LA cannot see some basic properties of the application
            await additionalApplications.tabNavigation('Other applications');
            await expect(page.getByText('This is a confidential application and restricted viewing applies')).toBeVisible();

            // LA cannot see the draft order to be approved
            await additionalApplications.tabNavigation('Draft orders');
            await expect(page.getByText('This is a confidential draft order and restricted viewing applies')).toBeVisible();
        });
});
