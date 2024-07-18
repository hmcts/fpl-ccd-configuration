import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/caseInPrepareForHearing.json' assert { type: "json" };
import { CTSCUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser } from "../settings/user-credentials";
import { expect } from "@playwright/test";
import { testConfig } from "../settings/test-config";
import { setHighCourt } from '../utils/update-case-details';

test.describe('Return application', () => {
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let caseName: string;
  test.beforeEach(async () => {
    caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });

  test('CTSC return Application',
    async ({ page, signInPage, returnApplication }) => {
      caseName = 'CTSC return Application ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, returnApplication,);
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);
      await returnApplication.gotoNextStep('Return application');

      //complete task
      await page.getByRole('button', { name: 'Go' }).click
      await page.getByLabel('Application Incomplete').check();
      await page.getByLabel('Let the local authority know').click();
      await page.getByLabel('Let the local authority know').fill('test');
      await page.getByRole('button', { name: 'Submit' }).click();
      await page.getByRole('button', { name: 'Save and continue' }).click();

    });

  test('LA submit application',
    async ({ page, signInPage, submitCase }) => {
      caseName = 'LA submit application ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData,);
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await submitCase.gotoNextStep('Submit application')

    });
});
  