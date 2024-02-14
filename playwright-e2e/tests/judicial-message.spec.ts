import { test} from '../fixtures/create-fixture';
import {Apihelp} from '../utils/api-helper';
//import {urlConfig} from "../settings/urls";
import caseData from '../caseData/mandatorySubmissionFields.json';
import caseDataJudgeMessage from '../caseData/caseWithJudgeMessage.json';
import caseDataCloseMessage from '../caseData/caseWithJudicialMessageReply.json';
import { newSwanseaLocalAuthorityUserOne,CTSCUser ,judgeUser} from '../settings/user-credentials';
import { expect } from '@playwright/test';


test.describe('send and reply message',()=>{
  let apiDataSetup = new Apihelp();
  const dateTime = new Date().toISOString();
  let caseNumber : string;
  let casename : string;
  test.beforeEach(async ()  => {
      caseNumber =  await apiDataSetup.createCase('e2e case',newSwanseaLocalAuthorityUserOne);
  });

  test('CTSC admin send message to Judge',
    async ({page,signInPage,judicialMessages}) => {
        casename = 'CTSC message Judge ' + dateTime.slice(0, 10);
        await apiDataSetup.updateCase(casename,caseNumber,caseData);
        await  signInPage.visit();
        await signInPage.login(CTSCUser.email,CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Send messages');
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
