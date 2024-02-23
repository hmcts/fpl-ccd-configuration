import {test} from '../fixtures/create-fixture';
import {Apihelp} from '../utils/api-helper';
import caseData from '../caseData/mandatorySubmissionFields.json';
import caseDataJudgeMessage from '../caseData/caseWithJudgeMessage.json';
import {
  newSwanseaLocalAuthorityUserOne,
  judgeWalesUser,
  judgeMidlandsUser
} from '../settings/user-credentials';
import {expect} from '@playwright/test';
import {caseNumberHyphenated} from "../utils/case-number";
import { testConfig } from '../settings/test-config';


test.describe('Regional case access', () => {
  test.skip(!testConfig.waEnabled); // Skip tests if WA is disabled, as they can't be tested
  
  let apiDataSetup = new Apihelp();
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let casename: string;
  test.beforeEach(async () => {
    caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });

  test('Welsh Judge can see Welsh case',
    async ({page, signInPage}) => {
      casename = 'Welsh judge, Welsh case visibility ' + dateTime.slice(0, 10);
      await apiDataSetup.updateCase(casename, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);
      await expect(page.getByText(casename)).toBeVisible();
    });

  // Dependent on DFPL-2107 - Regional access to cases re-enabled
  test('Midlands Judge has to request challenged access', async ({page, signInPage, challengedAccess }) => {
    casename = 'Midlands judge, Welsh case visibility ' + dateTime.slice(0, 10);
    await apiDataSetup.updateCase(casename, caseNumber, caseDataJudgeMessage);
    await signInPage.visit();
    await signInPage.login(judgeMidlandsUser.email, judgeMidlandsUser.password);
    await signInPage.navigateTOCaseDetails(caseNumber);

    await expect(page.getByText("This case requires challenged access.")).toBeVisible();
    await expect(page.getByText(`#${caseNumberHyphenated(caseNumber)}`)).toBeVisible();

    await challengedAccess.requestAccessToCase();
    await challengedAccess.chooseReason();
    await challengedAccess.viewCase();

    await expect(page.getByText(casename)).toBeVisible();
  });

});
