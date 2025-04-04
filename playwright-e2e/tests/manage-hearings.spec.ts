import { test } from  '../fixtures/create-fixture';
import caseData from '../caseData/caseWithHearingDetails.json' assert { type: 'json' };
import vacatedHearingCaseData from '../caseData/caseWithVacatedHearing.json' assert { type: 'json' };
import preJudgeAllocationCaseData from '../caseData/casePreAllocationDecision.json' assert { type: 'json' };
import {
  CTSCUser,
  newSwanseaLocalAuthorityUserOne,
  judgeWalesUser
} from "../settings/user-credentials";
import {expect} from "@playwright/test";
import {testConfig} from "../settings/test-config";
import {createCase, updateCase} from "../utils/api-helper";

test.describe('manage hearings', () => {
  const dateTime = new Date().toISOString();
  let caseNumber : string;
  let caseName : string;
  test.beforeEach(async ()  => {
    caseNumber =  await createCase('e2e case',newSwanseaLocalAuthorityUserOne);
  });

  test('CTSC admin adds new hearing',
    async ({ctscUser,manageHearings}) => {
      caseName = 'CTSC manage hearings ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await manageHearings.switchUser(ctscUser.page);
      await manageHearings.navigateTOCaseDetails(caseNumber);
      await manageHearings.gotoNextStep('Manage hearings');
      await manageHearings.createNewHearingOnCase();
      await expect(manageHearings.page.getByText('has been updated with event: Manage hearings')).toBeVisible();
    });

  test('CTSC admin edits a hearing that has taken place',
    async({ctscUser,manageHearings}) => {
      caseName = 'CTSC admin edits a hearing that has taken place ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await manageHearings.switchUser(ctscUser.page);
      await manageHearings.navigateTOCaseDetails(caseNumber);
      await manageHearings.gotoNextStep('Manage hearings')
      await manageHearings.editPastHearingOnCase();
      await expect(manageHearings.page.getByText('has been updated with event: Manage hearings')).toBeVisible();
    });

  test('CTSC admin edits future hearing',
    async({ctscUser,manageHearings}) => {
      caseName = 'CTSC admin edits future hearing ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await manageHearings.switchUser(ctscUser.page);
      await manageHearings.navigateTOCaseDetails(caseNumber);
      await manageHearings.gotoNextStep('Manage hearings')
      await manageHearings.editFutureHearingOnCase('Further case management hearing, 1 January 2050');
      await expect(manageHearings.page.getByText('has been updated with event: Manage hearings')).toBeVisible();
    });

  test('CTSC admin updates future hearing judge',
    async({ctscUser,signInPage,page,gateKeepingListing,
            manageHearings, caseDetails}) => {
      test.skip(!testConfig.waEnabled, 'This test should only run when work allocation has been enabled');
      caseName = 'CTSC admin edits future hearing judge ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, preJudgeAllocationCaseData);
      await manageHearings.switchUser(ctscUser.page);
      await manageHearings.navigateTOCaseDetails(caseNumber);
      await gateKeepingListing.gotoNextStep('Judicial Gatekeeping');
      await gateKeepingListing.completeJudicialGatekeeping();
      await gateKeepingListing.gotoNextStep('List Gatekeeping Hearing');
      await gateKeepingListing.addAllocatedJudgeAndCompleteGatekeepingListing();
    //  await gateKeepingListing.clickSignOut();
      await signInPage.visit();
      await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);
      await caseDetails.switchUser(page);
      await caseDetails.tabNavigation('Roles and access');
      const todaysDate = new Date().toLocaleDateString('en-GB',
        {day: 'numeric', month: 'long', year: 'numeric'});
      const expectedRows = [
        ['District Judge (MC) Craig Taylor', 'Allocated Judge', todaysDate, ''],
        ['District Judge (MC) Craig Taylor', 'Hearing Judge', '1 June 2024', '1 January 2050']
      ];
      await caseDetails.validateRolesAndAccessTab(expectedRows, '1 June 2024');
      await manageHearings.switchUser(page);
      await manageHearings.gotoNextStep('Manage hearings')
      await manageHearings.page.getByText('Edit a future hearing').click();
      await manageHearings.editFutureHearingOnCase('Case management hearing, 1 June 2024',
        'His Honour Judge Arthur Ramirez');
      await expect(manageHearings.page.getByText('has been updated with event: Manage hearings')).toBeVisible();
      await caseDetails.tabNavigation('Roles and access');
      const expectedRowsPostAmendHearingJudge = [
        ['District Judge (MC) Craig Taylor', 'Allocated Judge', todaysDate, ''],
        ['His Honour Judge Arthur Ramirez', 'Hearing Judge', '1 June 2024', '1 January 2050']
      ];
      await caseDetails.validateRolesAndAccessTab(expectedRowsPostAmendHearingJudge,
        'His Honour Judge Arthur Ramirez');
  });

  test('CTSC admin vacates a hearing',
    async({page,signInPage,manageHearings,ctscUser}) => {
      caseName = 'CTSC admin vacates a hearing ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
        await manageHearings.switchUser(ctscUser.page);
        await manageHearings.navigateTOCaseDetails(caseNumber);
      await manageHearings.gotoNextStep('Manage hearings')
      await manageHearings.vacateHearing();
      await expect(manageHearings.page.getByText('has been updated with event: Manage hearings')).toBeVisible();
    });

  test('CTSC admin adjourns a hearing',
    async({page,signInPage,manageHearings,ctscUser}) => {
      caseName = 'CTSC admin adjourns a hearing ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await manageHearings.switchUser(ctscUser.page);
      await manageHearings.navigateTOCaseDetails(caseNumber);
      await manageHearings.gotoNextStep('Manage hearings');
      await manageHearings.adjournHearing();
      await expect(manageHearings.page.getByText('has been updated with event: Manage hearings')).toBeVisible();
    });

  test('CTSC admin re-lists a hearing',
    async({ctscUser,manageHearings}) => {
      caseName = 'CTSC admin re-lists a hearing ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, vacatedHearingCaseData);
      await manageHearings.switchUser(ctscUser.page);
      await manageHearings.navigateTOCaseDetails(caseNumber);
      await manageHearings.gotoNextStep('Manage hearings')
      await manageHearings.reListHearing();
      await expect(manageHearings.page.getByText('has been updated with event: Manage hearings')).toBeVisible();
    });
});
