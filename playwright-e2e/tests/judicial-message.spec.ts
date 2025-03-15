import { test} from '../fixtures/create-fixture';
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: 'json' };
import caseDataJudgeMessage from '../caseData/caseWithJudgeMessage.json' assert { type: 'json' };
import caseDataCloseMessage from '../caseData/caseWithJudicialMessageReply.json' assert { type: 'json' };
import { newSwanseaLocalAuthorityUserOne,CTSCUser ,judgeUser} from '../settings/user-credentials';
import { expect } from '@playwright/test';
import {createCase, updateCase} from "../utils/api-helper";


test.describe('send and reply message',()=>{
  const dateTime = new Date().toISOString();
  let caseNumber : string;
  let casename : string;
  test.beforeEach(async ()  => {
      caseNumber =  await createCase('e2e case',newSwanseaLocalAuthorityUserOne);
  });

  test('CTSC admin send message to Judge with application',
    async ({page,signInPage,judicialMessages}) => {
        casename = 'CTSC message Judge ' + dateTime.slice(0, 10);
        await updateCase(casename,caseNumber,caseData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email,CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Send messages');
        await judicialMessages.sendMessageToAllocatedJudgeWithApplication();
        await judicialMessages.checkYourAnsAndSubmit();
        await judicialMessages.tabNavigation('Judicial messages');
        await expect(page.getByText('Message 1',{exact:true})).toBeVisible();
        await expect(page.getByText('Allocated Judge/Legal Adviser')).toBeVisible();
        await expect(page.getByRole('cell', { name: 'CTSC - message send to allocated Judge', exact: true })).toBeVisible();
  });

    test('CTSC admin send message to Judge with document',
    async ({page,signInPage,judicialMessages}) => {
        casename = 'CTSC message Judge ' + dateTime.slice(0, 10);
        await updateCase(casename,caseNumber,caseData);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email,CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Send messages');
        await judicialMessages.sendMessageToAllocatedJudgeWithDocument();
        await judicialMessages.checkYourAnsAndSubmit();
        await judicialMessages.tabNavigation('Judicial messages');
        await expect(page.getByText('Message 1',{exact:true})).toBeVisible();
        await expect(page.getByText('Allocated Judge/Legal Adviser')).toBeVisible();
        await expect(page.getByRole('cell', { name: 'CTSC - message send to allocated Judge', exact: true })).toBeVisible();
    });

    test('Judge reply CTCS message',async({page,signInPage,judicialMessages})=>{
        casename = 'Judge Reply ' + dateTime.slice(0, 10);
        await updateCase(casename,caseNumber,caseDataJudgeMessage);
        await  signInPage.visit();
        await signInPage.login(judgeUser.email,judgeUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Reply to messages');
        await judicialMessages.judgeReplyMessage();
        await judicialMessages.checkYourAnsAndSubmit();
        await judicialMessages.tabNavigation('Judicial messages');
        await expect(page.getByRole('cell', { name: 'FamilyPublicLaw+ctsc@gmail.com - Some note Other Judge/Legal Adviser (judiciary-only@mailnesia.com) - Reply CTSC admin about the hearing.', exact: true })).toBeVisible();
        await expect(page.getByText('Reply CTSC admin about the hearing.', { exact: true })).toBeVisible();
    });

    test('CTSC admin close the Message',async({page,signInPage,judicialMessages}) =>{
      casename = 'CTSC Admin Close Message ' + dateTime.slice(0, 10);
      await updateCase(casename,caseNumber,caseDataCloseMessage);
      await signInPage.visit();
      await signInPage.login(CTSCUser.email,CTSCUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);
      await judicialMessages.gotoNextStep('Reply to messages');
      await judicialMessages.CTSCUserCloseMessage();
      await judicialMessages.checkYourAnsAndSubmit();
      await judicialMessages.tabNavigation('Judicial messages');
      await expect(page.getByRole('cell', { name: 'Closed', exact: true })).toBeVisible();
    })

});
