import { test} from '../fixtures/create-fixture';
import {Apihelp} from '../utils/api-helper';
//import {urlConfig} from "../settings/urls";
import caseData from '../caseData/mandatorySubmissionFields.json';
import { newSwanseaLocalAuthorityUserOne,CTSCUser ,judgeUser} from '../settings/user-credentials';
import { expect } from '@playwright/test';


test.describe('Manage case linking',()=>{
  let apiDataSetup = new Apihelp();
  const dateTime = new Date().toISOString();
  let caseNumber : string;
  let casename : string;
  let linkedCase1: string;
  let linkedCase2: string;
  let linkedCase3: string;
  test.beforeEach(async ()  => {
      caseNumber =  await apiDataSetup.createCase('e2e case',newSwanseaLocalAuthorityUserOne);
      linkedCase1  = await apiDataSetup.createCase('e2e case',newSwanseaLocalAuthorityUserOne);
      linkedCase2  = await apiDataSetup.createCase('e2e case',newSwanseaLocalAuthorityUserOne);
      linkedCase3  = await apiDataSetup.createCase('e2e case',newSwanseaLocalAuthorityUserOne);
  });

  test('CTSC user lilnk cases @local',
    async ({page,signInPage,caseLink}) => {
        casename = 'CTSC admin link cases ' + dateTime.slice(0, 10);
        await apiDataSetup.updateCase(casename,caseNumber,caseData);
        await apiDataSetup.updateCase("linkedCase1",linkedCase1,caseData);
        await apiDataSetup.updateCase("linkedCase2",linkedCase2,caseData);
        await apiDataSetup.updateCase("linkedCase3",linkedCase3,caseData);
        console.log ("\nlinkedCase1 =" + linkedCase1);
        console.log ("\nlinkedCase2 =" + linkedCase2);
        console.log ("\nlinkedCase3 =" + linkedCase3);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email,CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await caseLink.gotoNextStep('Link cases');
       // await page.pause();
        await caseLink.clickNext();
        await caseLink.proposeCaseLink(linkedCase1,['Case consolidated','Linked for a hearing','Same child/ren']);
        await expect(page.getByText(caseLink.hypenateCaseNumber(linkedCase1) )).toBeVisible();
        await caseLink.proposeCaseLink(linkedCase2,['Case consolidated']);
        await expect(page.getByText(caseLink.hypenateCaseNumber(linkedCase2) )).toBeVisible();
        await caseLink.proposeCaseLink(linkedCase3,['Linked for a hearing']);
        await expect(page.getByText(caseLink.hypenateCaseNumber(linkedCase3) )).toBeVisible();
        await caseLink.clickNext();
        await caseLink.checkYourAnsAndSubmit();
        await caseLink.tabNavigation("History");
          await page.getByText('History', { exact: true }).click()
         await expect(page.getByLabel('you are on event Link cases')).toContainText('Link cases');
          await caseLink.tabNavigation("Linked Cases");
        await expect(page.getByLabel('Linked Cases').getByRole('paragraph')).toContainText('Case name missing ' + caseLink.hypenateCaseNumber(linkedCase1));



    });



});
