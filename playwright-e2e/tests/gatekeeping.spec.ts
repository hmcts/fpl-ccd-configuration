import { test } from  '../fixtures/create-fixture';
import {Apihelp} from '../utils/api-helper';
import caseData from '../caseData/mandatorySubmissionFields.json';
import {
  CTSCUser,
  newSwanseaLocalAuthorityUserOne,
  HighCourtAdminUser
} from "../settings/user-credentials";
import {expect} from "@playwright/test";
import {testConfig} from "../settings/test-config";
import { setHighCourt } from '../utils/update-case-details';

test.describe('gatekeeping', () => {
  let apiDataSetup = new Apihelp();
  const dateTime = new Date().toISOString();
  let caseNumber : string;
  let caseName : string;
  test.beforeEach(async ()  => {
    caseNumber =  await apiDataSetup.createCase('e2e case',newSwanseaLocalAuthorityUserOne);
  });

  test('Review Standard Direction Order High Court WA Task',
    async({page,signInPage,gateKeepingListing,
            caseFileView}) => {
      test.skip(!testConfig.waEnabled, 'This test should only run when work allocation has been enabled');
      caseName = 'Review Standard Direction Order High Court WA Task ' + dateTime.slice(0, 10);
      setHighCourt(caseData);
      await apiDataSetup.updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password)
      await signInPage.navigateTOCaseDetails(caseNumber);
      
      await gateKeepingListing.gotoNextStep('Judicial Gatekeeping');
      await gateKeepingListing.completeJudicialGatekeepingWithUploadedOrder();
      await gateKeepingListing.gotoNextStep('List Gatekeeping Hearing');
      await gateKeepingListing.addHighCourtJudgeAndCompleteGatekeepingLists(); 

      //Check CFV
      await caseFileView.goToCFVTab();
      await caseFileView.openFolder('Orders');
      await expect(page.getByRole('tree')).toContainText('textfile.pdf');
      await gateKeepingListing.clickSignOut();

      //Test WA Task exists
      await signInPage.visit();
      await signInPage.login(HighCourtAdminUser.email, HighCourtAdminUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);
      await gateKeepingListing.tabNavigation('Tasks');
      await gateKeepingListing.waitForTask('Review Standard Direction Order (High Court)');

      // Assign and complete the task
      await page.getByText('Assign to me').click();
      await page.getByText('Mark as done').click();
      await page.getByRole('button', { name: "Mark as done" }).click();

      // Should be no more tasks on the page
      await expect(page.getByText('Review Correspondence (High Court')).toHaveCount(0);
  });
});
