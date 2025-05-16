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

test.describe('Return application @sessionreuse', () => {
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let caseName: string;
  test.beforeEach(async () => {
    caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);

  });

  test('CTSC return Application',
    async ({ ctscUser, returnApplication }) => {
      caseName = 'CTSC return Application ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await returnApplication.switchUser(ctscUser.page);
      await returnApplication.navigateTOCaseDetails(caseNumber);
      await returnApplication.gotoNextStep('Return application');
      await returnApplication.ReturnApplication();

     // complete task
      //await returnApplication.tabNavigation('History');
       await returnApplication.page.getByRole('tab', { name: 'History',exact:true }).click();
      await expect(returnApplication.page.getByText('Returned')).toBeVisible();

    });

  test('LA submit application',
    async ({ localAuthorityUser, returnApplication }) => {
      caseName = 'LA submit application ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, returnedCase);
      await returnApplication.switchUser(localAuthorityUser.page);
      await returnApplication.navigateTOCaseDetails(caseNumber);
      await returnApplication.page.getByRole('link', { name: 'Make changes to the respondents\' details' }).click();
      await returnApplication.UpdateRespondent();

   });
})
