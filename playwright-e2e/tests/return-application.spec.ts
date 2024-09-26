import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/mandatorySubmissionFields.json' with { type: "json" };
import returnedCase from '../caseData/returnCase.json' with { type: "json" };
import { CTSCUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser } from "../settings/user-credentials";
import { expect } from "@playwright/test";
import { testConfig } from "../settings/test-config";
import { setHighCourt } from '../utils/update-case-details';
import { SubmitCase } from '../pages/submit-case';
import { ReturnApplication } from '../pages/return-application';

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
      await updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);
      await returnApplication.gotoNextStep('Return application');
      await returnApplication.ReturnApplication();

      //complete task
        await returnApplication.tabNavigation('History');
        await expect(page.getByText('Returned')).toBeVisible();
        await signInPage.logout();

    });

  test('LA submit application',
    async ({ page, signInPage, returnApplication}) => {
      caseName = 'LA submit application ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, returnedCase);
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateTOCaseDetails(caseNumber);
      await returnApplication.gotoNextStep('Submit application');
      await returnApplication.SubmitApplication();

      //complete task
      await returnApplication.tabNavigation('History');
      await expect(page.getByText('Submitted')).toBeVisible();
      await signInPage.logout();
 
    });
})