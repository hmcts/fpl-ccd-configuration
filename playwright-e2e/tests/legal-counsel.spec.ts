import { test} from '../fixtures/create-fixture';
import {Apihelp} from '../utils/api-helper';
//import {urlConfig} from "../settings/urls";
import caseData from '../caseData/mandatorySubmissionFields.json';
import caseDataJudgeMessage from '../caseData/caseWithJudgeMessage.json';
import caseDataCloseMessage from '../caseData/caseWithJudicialMessageReply.json';
import caseWithResSolicitor from '../caseData/caseWithRespondentSolicitor.json';
import { newSwanseaLocalAuthorityUserOne,CTSCUser ,judgeUser} from '../settings/user-credentials';
import { expect } from '@playwright/test';


test.describe('Respondent solicitor add legal counsel ',
    () => {
        let apiDataSetup = new Apihelp();
        const dateTime = new Date().toISOString();
        let caseNumber: string;
        let casename: string;
        test.beforeEach(async () => {
            caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        });

        test('CTSC admin send message to Judge',
            async ({page, signInPage, judicialMessages}) => {
                casename = 'CTSC message Judge ' + dateTime.slice(0, 10);
                // await apiDataSetup.updateCase(casename, caseNumber, caseWithResSolicitor);
                 await apiDataSetup.giveAccessToCase('1711405300614392');
                 console.log ("case" + caseNumber);
                 await signInPage.visit();
                 await signInPage.login(CTSCUser.email, CTSCUser.password);
                 await signInPage.navigateTOCaseDetails(caseNumber);
                // await judicialMessages.gotoNextStep('Send messages');
                // await judicialMessages.sendMessageToAllocatedJudge();
                // await judicialMessages.checkYourAnsAndSubmit();
                // await judicialMessages.tabNavigation('Judicial messages');
                // await expect(page.getByText('FamilyPublicLaw+ctsc@gmail.com - Message send to Allocated Judge')).toBeVisible();
           
            });

        test.skip('Judge reply CTCS message', async ({page, signInPage, judicialMessages}) => {
                   });

        test.skip('CTSC admin close the Message', async ({page, signInPage, judicialMessages}) => {
                    })

    });
