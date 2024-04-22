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
        await page.pause();
        await caseLink.proposeCaseLink(linkedCase1,['Case consolidated','Linked for a hearing','Same child/ren']);
        await expect(page.getByText(linkedCase1 )).toBeVisible();
        await caseLink.proposeCaseLink(linkedCase2,['Case consolidated']);
        await expect(page.getByText('Case name missing 1713-5443-5562-')).toBeVisible();
        await caseLink.proposeCaseLink(linkedCase3,['Linked for a hearing']);
        await expect(page.getByText('Case name missing 1713-5443-5562-')).toBeVisible();



        await page.getByLabel('Same child/ren').check();
        await page.getByRole('button', { name: 'Propose case link' }).click();
        //
        await page.getByRole('button', { name: 'Next' }).click();
        // await expect(page.getByRole('heading', { name: 'Check your answers' })).toBeVisible();
        await page.getByRole('button', { name: 'Submit' }).click();
        await page.goto('https://manage-case.demo.platform.hmcts.net/cases/case-details/1713544297954516');
        await page.goto('https://manage-case.demo.platform.hmcts.net/cases/case-details/1713544297954516#Summary');
        await page.getByText('Linked Cases').click();
        // await expect(page.locator('ccd-linked-cases-to-table')).toContainText('Case consolidated7193');
        await page.getByLabel('case viewer table').locator('div').filter({ hasText: 'Linked casesThis case is' }).first().click();
        // await expect(page.locator('ccd-linked-cases-to-table')).toContainText('Case name missing 1713-5443-1992-7193');
        const page1Promise = page.waitForEvent('popup');
        await page.getByRole('link', { name: 'Case name missing 1713-5443-3971-' }).click();
        const page1 = await page1Promise;
        await page1.close();
        // await expect(page.locator('ccd-linked-cases-to-table')).toContainText('Case name missing 1713-5443-3971-9182');
        // await expect(page.locator('ccd-linked-cases-to-table')).toContainText('Case name missing 1713-5443-5562-1312');

        // ---------------------
        await context.close();
        await browser.close();

        await judicialMessages.sendMessageToAllocatedJudge();
        await judicialMessages.checkYourAnsAndSubmit();
        await judicialMessages.tabNavigation('Judicial messages');
        await expect(page.getByText('FamilyPublicLaw+ctsc@gmail.com - Message send to Allocated Judge')).toBeVisible();
    });


    test('Judge reply CTCS message',async({page,signInPage,judicialMessages})=>{
        casename = 'Judge Reply ' + dateTime.slice(0, 10);
        await apiDataSetup.updateCase(casename,caseNumber,caseDataJudgeMessage);
        await  signInPage.visit();
        await signInPage.login(judgeUser.email,judgeUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Reply to messages');
        await judicialMessages.judgeReplyMessage();
        await judicialMessages.checkYourAnsAndSubmit();
        await judicialMessages.tabNavigation('Judicial messages');
        await expect(page.getByText('FamilyPublicLaw+ctsc@gmail.com - Some note judiciary-only@mailnesia.com - Reply CTSC admin about the hearing.')).toBeVisible();
    });

    test('CTSC admin close the Message',async({page,signInPage,judicialMessages}) =>{
      casename = 'CTSC Admin Close Message ' + dateTime.slice(0, 10);
      await apiDataSetup.updateCase(casename,caseNumber,caseDataCloseMessage);
      await  signInPage.visit();
      await signInPage.login(CTSCUser.email,CTSCUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);
      await judicialMessages.gotoNextStep('Reply to messages');
      await judicialMessages.CTSCUserCloseMessage();
      await judicialMessages.checkYourAnsAndSubmit();
      await judicialMessages.tabNavigation('Judicial messages');
      await expect(page.getByRole('cell', { name: 'Closed', exact: true })).toBeVisible();
    })

});
