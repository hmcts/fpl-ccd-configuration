import { test} from '../fixtures/create-fixture';
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: 'json' };
import caseDataJudgeMessage from '../caseData/caseWithJudgeMessage.json' assert { type: 'json' };
import caseDataCloseMessage from '../caseData/caseWithJudicialMessageReply.json' assert { type: 'json' };
import { newSwanseaLocalAuthorityUserOne,CTSCUser ,judgeUser} from '../settings/user-credentials';
import { expect } from '@playwright/test';
import {createCase, updateCase} from "../utils/api-helper";
import {LegalUserPage} from "../pages/legal-user-browser";



test.describe('send and reply message',()=>{
  const dateTime = new Date().toISOString();
  let caseNumber : string;
  let casename : string;
  test.beforeEach(async ()  => {
      caseNumber =  await createCase('e2e case',newSwanseaLocalAuthorityUserOne);
  });

  test('CTSC admin send message to Judge with application',
    async ({judicialMessages,ctscUserPage}) => {
        casename = 'CTSC message Judge ' + dateTime.slice(0, 10);

        await updateCase(casename,caseNumber,caseData);

        await judicialMessages.switchUser(ctscUserPage.page);

        await judicialMessages.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Send messages');
        await judicialMessages.sendMessageToAllocatedJudgeWithApplication();
        await judicialMessages.checkYourAnsAndSubmit();
        await judicialMessages.tabNavigation('Judicial messages');
        await expect(ctscUserPage.page.getByText('FamilyPublicLaw+ctsc@gmail.com - Message send to Allocated Judge')).toBeVisible();
    });

    test('CTSC admin send message to Judge with document',
    async ({judicialMessages,ctscUserPage}) => {
        casename = 'CTSC message Judge ' + dateTime.slice(0, 10);
        await updateCase(casename,caseNumber,caseData);
        await judicialMessages.switchUser(ctscUserPage.page);
        await judicialMessages.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Send messages');
        await judicialMessages.sendMessageToAllocatedJudgeWithDocument();
        await judicialMessages.checkYourAnsAndSubmit();
        await judicialMessages.tabNavigation('Judicial messages');
        await expect(ctscUserPage.page.getByText('FamilyPublicLaw+ctsc@gmail.com - Message send to Allocated Judge')).toBeVisible();
    });

    test('Judge reply CTCS message',async({judicialMessages,legalUserPage})=>{
        casename = 'Judge Reply ' + dateTime.slice(0, 10);
        await updateCase(casename,caseNumber,caseDataJudgeMessage);
        console.log(await legalUserPage.page.context().storageState());
        await judicialMessages.switchUser(legalUserPage.page);
        // await  signInPage.visit();
        // await signInPage.login(judgeUser.email,judgeUser.password);
        await judicialMessages.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Reply to messages');
        await judicialMessages.judgeReplyMessage();
        await judicialMessages.checkYourAnsAndSubmit();
        await judicialMessages.tabNavigation('Judicial messages');
        await expect(legalUserPage.page.getByText('FamilyPublicLaw+ctsc@gmail.com - Some note judiciary-only@mailnesia.com - Reply CTSC admin about the hearing.')).toBeVisible();
    });

    test('CTSC admin close the Message',async({judicialMessages,ctscUserPage}) =>{
      casename = 'CTSC Admin Close Message ' + dateTime.slice(0, 10);
      await updateCase(casename,caseNumber,caseDataCloseMessage);
      // await signInPage.visit();
      // await signInPage.login(CTSCUser.email,CTSCUser.password);
        await judicialMessages.switchUser(ctscUserPage.page);
      await judicialMessages.navigateTOCaseDetails(caseNumber);
      await judicialMessages.gotoNextStep('Reply to messages');
      await judicialMessages.CTSCUserCloseMessage();
      await judicialMessages.checkYourAnsAndSubmit();
      await judicialMessages.tabNavigation('Judicial messages');
      await expect(ctscUserPage.page.getByRole('cell', { name: 'Closed', exact: true })).toBeVisible();
    })

});
