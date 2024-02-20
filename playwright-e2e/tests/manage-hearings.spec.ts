import { test } from  '../fixtures/create-fixture';
import {Apihelp} from '../utils/api-helper';
import caseData from '../caseData/caseWithHearingDetails.json';
import vacatedHearingCaseData from '../caseData/caseWithVacatedHearing.json';
import {CTSCUser, newSwanseaLocalAuthorityUserOne} from "../settings/user-credentials";
import {expect} from "@playwright/test";

test.describe('manage hearings', () => {
  let apiDataSetup = new Apihelp();
  const dateTime = new Date().toISOString();
  let caseNumber : string;
  let caseName : string;
  test.beforeEach(async ()  => {
    caseNumber =  await apiDataSetup.createCase('e2e case',newSwanseaLocalAuthorityUserOne);
  });

  test('CTSC admin adds new hearing',
    async ({page,signInPage,manageHearings}) => {
      caseName = 'CTSC manage hearings ' + dateTime.slice(0, 10);
      await apiDataSetup.updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);
      await manageHearings.gotoNextStep('Manage hearings');
      await manageHearings.createNewHearingOnCase();
      await expect(page.getByText('has been updated with event: Manage hearings')).toBeVisible();
    });

  test('CTSC admin edits a hearing that has taken place',
    async({page,signInPage,manageHearings}) => {
      caseName = 'CTSC admin edits a hearing that has taken place ' + dateTime.slice(0, 10);
      await apiDataSetup.updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password)
      await signInPage.navigateTOCaseDetails(caseNumber);
      await manageHearings.gotoNextStep('Manage hearings')
      await manageHearings.editPastHearingOnCase();
      await expect(page.getByText('has been updated with event: Manage hearings')).toBeVisible();
    });

  test('CTSC admin edits future hearing',
    async({page,signInPage,manageHearings}) => {
      caseName = 'CTSC admin edits future hearing ' + dateTime.slice(0, 10);
      await apiDataSetup.updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password)
      await signInPage.navigateTOCaseDetails(caseNumber);
      await manageHearings.gotoNextStep('Manage hearings')
      await manageHearings.editFutureHearingOnCase();
      await expect(page.getByText('has been updated with event: Manage hearings')).toBeVisible();
  });

  test('CTSC admin vacates a hearing',
    async({page,signInPage,manageHearings}) => {
      caseName = 'CTSC admin vacates a hearing ' + dateTime.slice(0, 10);
      await apiDataSetup.updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password)
      await signInPage.navigateTOCaseDetails(caseNumber);
      await manageHearings.gotoNextStep('Manage hearings')
      await manageHearings.vacateHearing();
      await expect(page.getByText('has been updated with event: Manage hearings')).toBeVisible();
    });

  test('CTSC admin adjourns a hearing',
    async({page,signInPage,manageHearings}) => {
      caseName = 'CTSC admin adjourns a hearing ' + dateTime.slice(0, 10);
      await apiDataSetup.updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password)
      await signInPage.navigateTOCaseDetails(caseNumber);
      await manageHearings.gotoNextStep('Manage hearings')
      await manageHearings.adjournHearing();
      await expect(page.getByText('has been updated with event: Manage hearings')).toBeVisible();
    });

  test('CTSC admin re-lists a hearing',
    async({page,signInPage,manageHearings}) => {
      caseName = 'CTSC admin re-lists a hearing ' + dateTime.slice(0, 10);
      await apiDataSetup.updateCase(caseName, caseNumber, vacatedHearingCaseData);
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password)
      await signInPage.navigateTOCaseDetails(caseNumber);
      await manageHearings.gotoNextStep('Manage hearings')
      await manageHearings.reListHearing();
      await expect(page.getByText('has been updated with event: Manage hearings')).toBeVisible();
    });
});
