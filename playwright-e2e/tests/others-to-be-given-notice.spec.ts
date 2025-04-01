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
        async ({ ctscUser, othersToBeGivenNotice }) => {
            casename = 'CTSC added other person to case ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseData);
            await othersToBeGivenNotice.switchUser(ctscUser.page);
            await othersToBeGivenNotice.navigateTOCaseDetails(caseNumber);


            await othersToBeGivenNotice.gotoNextStep('Others to be given notice');
            await othersToBeGivenNotice.othersToBeGivenNotice();
            await othersToBeGivenNotice.tabNavigation('People in the case');
            await expect(othersToBeGivenNotice.page .getByText('Other person 1',{exact: true})).toBeVisible();
        })
    });
