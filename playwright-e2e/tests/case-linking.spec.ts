import { test} from '../fixtures/create-fixture';
import {Apihelp} from '../utils/api-helper';
//import {urlConfig} from "../settings/urls";
import caseData from "../caseData/mandatorySubmissionFields.json";
import linkedCaseData from "../caseData/caseWithLinkedCase.json"
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
  let updatedlinkedCase: string;
  test.beforeEach(async ()  => {
     // caseNumber =  await apiDataSetup.createCase('e2e case',newSwanseaLocalAuthorityUserOne);
      // linkedCase1  = await apiDataSetup.createCase('e2e case',newSwanseaLocalAuthorityUserOne);
      //  linkedCase2  = await apiDataSetup.createCase('e2e case',newSwanseaLocalAuthorityUserOne);
      //  linkedCase3  = await apiDataSetup.createCase('e2e case',newSwanseaLocalAuthorityUserOne);
  });

  test('CTSC user  link cases ',
    async ({page,signInPage,caseLink}) => {
        casename = 'CTSC admin link cases ' + dateTime.slice(0, 10);
        await apiDataSetup.updateCase(casename,caseNumber,caseData);
        await apiDataSetup.updateCase("linkedCase1",linkedCase1,caseData);
        await apiDataSetup.updateCase("linkedCase2",linkedCase2,caseData);
        await apiDataSetup.updateCase("linkedCase3",linkedCase3,caseData);
        // console.log ("\nlinkedCase1 =" + linkedCase1);
        // console.log ("\nlinkedCase2 =" + linkedCase2);
        // console.log ("\nlinkedCase3 =" + linkedCase3);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email,CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await caseLink.gotoNextStep('Link cases');
        await caseLink.clickNext();
        await caseLink.proposeCaseLink(linkedCase1,['Case consolidated','Linked for a hearing','Same child/ren']);
        await expect(page.getByText(caseLink.hypenateCaseNumber(linkedCase1) )).toBeVisible();
        await caseLink.proposeCaseLink(linkedCase2,['Case consolidated']);
        await expect(page.getByText(caseLink.hypenateCaseNumber(linkedCase2) )).toBeVisible();
        await caseLink.proposeCaseLink(linkedCase3,['Linked for a hearing']);
        await expect(page.getByText(caseLink.hypenateCaseNumber(linkedCase3) )).toBeVisible();
        await caseLink.clickNext();
        await caseLink.clickSubmit();
        await caseLink.tabNavigation("History");
        await expect(page.getByLabel('you are on event Link cases')).toContainText('Link cases');
        await caseLink.tabNavigation("Linked Cases");
        await expect(page.getByRole('link',{name:'Case name missing '+ caseLink.hypenateCaseNumber(linkedCase1)})).toBeVisible();
        await expect(page.getByRole('link',{name:'Case name missing '+ caseLink.hypenateCaseNumber(linkedCase2)})).toBeVisible();
        await expect(page.getByRole('link',{name:'Case name missing '+ caseLink.hypenateCaseNumber(linkedCase3)})).toBeVisible();
    });
    test('CTSC user manage linked cases @local', async ({page,signInPage,caseLink}) => {
        casename = 'CTSC admin manage cases ' + dateTime.slice(0, 10);
         // await apiDataSetup.updateCase("linkedCase1",linkedCase1,caseData);
         // await apiDataSetup.updateCase("linkedCase2",linkedCase2,caseData);
         // await apiDataSetup.updateCase("linkedCase3",linkedCase3,caseData);
        caseNumber='1714037627187807';
         await apiDataSetup.updateCase(casename,caseNumber,caseData);
        console.log('dfd' );
        linkedCase1 ='1714035740285599';
        linkedCase2 ='1714035754545052';
        linkedCase3='1714035768920374';

        // await apiDataSetup.updatelinkedCaseDetails(linkedCaseData, '1713888444305662', '1713888444305662', '1713888444305662');
         await apiDataSetup.updateLinkedCase(casename,caseNumber,linkedCaseData,linkedCase1,linkedCase2,linkedCase3);

        // console.log ("\nlinkedCase1 =" + linkedCase1);
        // console.log ("\nlinkedCase2 =" + linkedCase2);
        // console.log ("\nlinkedCase3 =" + linkedCase3);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email,CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        page.pause();
         await caseLink.gotoNextStep('Link cases');
         await caseLink.clickNext();
        // await caseLink.proposeCaseLink(linkedCase1,['Case consolidated','Linked for a hearing','Same child/ren']);
        // await expect(page.getByText(caseLink.hypenateCaseNumber(linkedCase1) )).toBeVisible();
        // await caseLink.proposeCaseLink(linkedCase2,['Case consolidated']);
        // await expect(page.getByText(caseLink.hypenateCaseNumber(linkedCase2) )).toBeVisible();
        // await caseLink.proposeCaseLink(linkedCase3,['Linked for a hearing']);
        // await expect(page.getByText(caseLink.hypenateCaseNumber(linkedCase3) )).toBeVisible();
        // await caseLink.clickNext();
        // await caseLink.clickSubmit();
        // await caseLink.tabNavigation("History");
        // await expect(page.getByLabel('you are on event Link cases')).toContainText('Link cases');
        // await caseLink.tabNavigation("Linked Cases");
        // await expect(page.getByRole('link',{name:'Case name missing '+ caseLink.hypenateCaseNumber(linkedCase1)})).toBeVisible();
        // await expect(page.getByRole('link',{name:'Case name missing '+ caseLink.hypenateCaseNumber(linkedCase2)})).toBeVisible();
        // await expect(page.getByRole('link',{name:'Case name missing '+ caseLink.hypenateCaseNumber(linkedCase3)})).toBeVisible();
    });



});
