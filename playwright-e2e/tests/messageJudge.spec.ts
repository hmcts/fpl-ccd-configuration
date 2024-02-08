import { test} from '../fixtures/create-fixture';
import {Apihelp} from '../utils/apiFixture';
import {UrlConfig} from "../settings/urls";
import caseData from '../caseData/mandatorySubmissionFields.json';
import caseDataJudgeMessage from '../caseData/caseWithJudgeMessage.json';
import { newSwanseaLocalAuthorityUserOne,CTSCUser ,judgeUser} from '../settings/userCredentials';
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
    async ({page,signInPage,sendMessage}) => {
        casename = 'CTSC message Judge' + dateTime.slice(0, 10);
        await apiDataSetup.updateCase(casename,caseNumber,caseData);
        await  signInPage.visit();
        await signInPage.login(CTSCUser.email,CTSCUser.password);
        await signInPage.navigateTOCaseDetails(`${UrlConfig.aatUrl}case-details/${caseNumber}`);
        await sendMessage.gotoNextStep('Send messages');
        await sendMessage.sendMessageToAllocatedJudge();
        await sendMessage.checkYourAnsAndSubmit();
        await sendMessage.tabNavigation('Judicial messages');
        await expect(page.getByText('FamilyPublicLaw+ctsc@gmail.com - Message send to Allocated Judge')).toBeVisible();
    });

    test('Judge reply CTCS message',async({page,signInPage,sendMessage})=>{
        casename = 'Judge Reply' + dateTime.slice(0, 10);
        await apiDataSetup.updateCase(casename,caseNumber,caseDataJudgeMessage);
        await  signInPage.visit();
        await signInPage.login(judgeUser.email,judgeUser.password);
        await  signInPage.navigateTOCaseDetails(`${UrlConfig.aatUrl}case-details/${caseNumber}`);
        await sendMessage.gotoNextStep('Reply to messages');
        await sendMessage.judgeReplyMessage();
        await sendMessage.checkYourAnsAndSubmit();
        await sendMessage.tabNavigation('Judicial messages');
        await expect(page.getByText('FamilyPublicLaw+ctsc@gmail.com - Some note judiciary-only@mailnesia.com - Reply CTSC admin about the hearing.')).toBeVisible();
    })

});







