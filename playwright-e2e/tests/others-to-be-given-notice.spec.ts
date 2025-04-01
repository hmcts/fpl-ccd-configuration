import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/caseSentToGatekeeper.json' assert { type: "json" };
import { newSwanseaLocalAuthorityUserOne, CTSCUser} from "../settings/user-credentials";
import { expect } from "@playwright/test";

test.describe('Others to be given notice', () => {
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let casename: string;

  test.beforeEach(async () => {
      caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });

    test('Others to be given notice',
        async ({ page, signInPage, othersToBeGivenNotice }) => {
            casename = 'CTSC added other person to case ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await signInPage.navigateTOCaseDetails(caseNumber);
            await othersToBeGivenNotice.gotoNextStep('Others to be given notice');
            await othersToBeGivenNotice.othersToBeGivenNotice();
            await othersToBeGivenNotice.tabNavigation('People in the case');
            await expect(page.getByText('Other people to be given notice 2',{exact: true})).toBeVisible();
        })
    });
