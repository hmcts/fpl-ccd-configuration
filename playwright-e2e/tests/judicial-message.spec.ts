import { test} from '../fixtures/create-fixture';
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: 'json' };
import caseDataJudgeMessage from '../caseData/caseWithJudgeMessage.json' assert { type: 'json' };
import caseDataCloseMessage from '../caseData/caseWithJudicialMessageReply.json' assert { type: 'json' };
import { newSwanseaLocalAuthorityUserOne} from '../settings/user-credentials';
import { expect } from '@playwright/test';
import {createCase, updateCase} from "../utils/api-helper";

test.describe('send and reply message @sessionreuse',()=>{
  const dateTime = new Date().toISOString();
  let caseNumber : string;
  let casename : string;
  test.beforeEach(async ()  => {
      caseNumber =  await createCase('e2e case',newSwanseaLocalAuthorityUserOne);
  });

  test('CTSC admin send message to Judge with application',
    async ({judicialMessages,ctscUser}) => {
        casename = 'CTSC message Judge ' + dateTime.slice(0, 10);

        await updateCase(casename,caseNumber,caseData);

        await judicialMessages.switchUser(ctscUser.page);

        await judicialMessages.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Send messages');
        await judicialMessages.sendMessageToAllocatedJudgeWithApplication();
        await judicialMessages.checkYourAnsAndSubmit();
        await judicialMessages.tabNavigation('Judicial messages');
        await expect(judicialMessages.page.getByText('FamilyPublicLaw+ctsc@gmail.com - Message send to Allocated Judge')).toBeVisible();

        await expect(judicialMessages.page.getByText('Message 1',{exact:true})).toBeVisible();
        await expect(judicialMessages.page.getByText('Allocated Judge/Legal Adviser')).toBeVisible();
        await expect(judicialMessages.page.getByRole('cell', { name: 'CTSC - message send to allocated Judge', exact: true })).toBeVisible();
  });

    test('CTSC admin send message to Judge with document',
    async ({judicialMessages,ctscUser}) => {
        casename = 'CTSC message Judge ' + dateTime.slice(0, 10);
        await updateCase(casename,caseNumber,caseData);
        await judicialMessages.switchUser(ctscUser.page);
        await judicialMessages.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Send messages');
        await judicialMessages.sendMessageToAllocatedJudgeWithDocument();
        await judicialMessages.checkYourAnsAndSubmit();
        await judicialMessages.tabNavigation('Judicial messages');
        await expect(judicialMessages.page.getByText('Message 1',{exact:true})).toBeVisible();
        await expect(judicialMessages.page.getByText('Allocated Judge/Legal Adviser')).toBeVisible();
        await expect(judicialMessages.page.getByRole('cell', { name: 'CTSC - message send to allocated Judge', exact: true })).toBeVisible();
    });

    test('Judge reply CTCS message',async({judicialMessages,legalUser})=>{
        casename = 'Judge Reply ' + dateTime.slice(0, 10);
        await updateCase(casename,caseNumber,caseDataJudgeMessage);
     //   console.log(await legalUser.page.context().storageState());
        await judicialMessages.switchUser(legalUser.page);

        await judicialMessages.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Reply to messages');
        await judicialMessages.judgeReplyMessage();
        await judicialMessages.checkYourAnsAndSubmit();
        await judicialMessages.tabNavigation('Judicial messages');
        await expect(legalUser.page.getByText('FamilyPublicLaw+ctsc@gmail.com - Some note judiciary-only@mailnesia.com - Reply CTSC admin about the hearing.')).toBeVisible();
        await expect(legalUser.page.getByRole('cell', { name: 'FamilyPublicLaw+ctsc@gmail.com - Some note Other Judge/Legal Adviser (judiciary-only@mailnesia.com) - Reply CTSC admin about the hearing.', exact: true })).toBeVisible();
        await expect(legalUser.page.getByText('Reply CTSC admin about the hearing.', { exact: true })).toBeVisible();
    });

    test('CTSC admin close the Message',async({judicialMessages,ctscUser}) =>{
      casename = 'CTSC Admin Close Message ' + dateTime.slice(0, 10);
      await updateCase(casename,caseNumber,caseDataCloseMessage);

        await judicialMessages.switchUser(ctscUser.page);
      await judicialMessages.navigateTOCaseDetails(caseNumber);
      await judicialMessages.gotoNextStep('Reply to messages');
      await judicialMessages.CTSCUserCloseMessage();
      await judicialMessages.checkYourAnsAndSubmit();
      await judicialMessages.tabNavigation('Judicial messages');
      await expect(ctscUser.page.getByRole('cell', { name: 'Closed', exact: true })).toBeVisible();
    })

});
