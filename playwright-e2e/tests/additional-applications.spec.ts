import { test } from '../fixtures/create-fixture';
import { Apihelp } from '../utils/api-helper';
import caseData from '../caseData/mandatorySubmissionFieldsWithoutAdditionalApp.json';
import { newSwanseaLocalAuthorityUserOne } from '../settings/user-credentials';
import { expect } from "@playwright/test";

test.describe('Upload additional applications', () => {
  let apiDataSetup = new Apihelp();
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let casename: string;

  test.beforeEach(async () => {
    caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });

  test('LA uploads a C1 application',
    async ({ page, signInPage, additionalApplications }) => {
      casename = 'LA uploads an other application ' + dateTime.slice(0, 10);
      await apiDataSetup.updateCase(casename, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseOtherApplicationType();
      await additionalApplications.fillOtherApplicationDetails();

      // Payment details
      await expect(page.getByText('£232.00')).toBeVisible();
      await additionalApplications.payForApplication();

      await additionalApplications.checkYourAnsAndSubmit();

      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText('PBA1234567')).toBeVisible();
      await expect(page.getByText('C1 - Change surname or remove from jurisdiction')).toBeVisible();
      await expect(page.getByText('On the same day')).toBeVisible();
    });

  test('LA uploads a C2 application with draft order',
    async ({ page, signInPage, additionalApplications }) => {
      casename = 'LA uploads a C2 application with draft order ' + dateTime.slice(0, 10);
      await apiDataSetup.updateCase(casename, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseC2ApplicationType();
      await additionalApplications.fillC2ApplicationDetails();

      // Payment details
      await expect(page.getByText('£232.00')).toBeVisible();
      await additionalApplications.payForApplication();

      await additionalApplications.checkYourAnsAndSubmit();

      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText('PBA1234567')).toBeVisible();
      await expect(page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
      await expect(page.getByText('On the same day')).toBeVisible();

      // can see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('Draft order title')).toBeVisible();
    });

});
