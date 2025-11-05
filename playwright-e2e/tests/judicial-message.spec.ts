import {test} from '../fixtures/create-fixture';
import caseData from '../caseData/mandatorySubmissionFields.json' assert {type: 'json'};
import caseDataJudgeMessage from '../caseData/caseWithJudgeMessage.json' assert {type: 'json'};
import caseDataCloseMessage from '../caseData/caseWithJudicialMessageReply.json' assert {type: 'json'};
import {newSwanseaLocalAuthorityUserOne, CTSCUser, judgeUser} from '../settings/user-credentials';
import {expect} from '@playwright/test';
import {createCase, updateCase} from "../utils/api-helper";
import {formatToLongDate12hrTime} from "../utils/util-helper";


test.describe('send and reply message', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('CTSC admin send message to Judge with application @xbrowser',
        async ({page, signInPage, judicialMessages}) => {
            casename = 'CTSC message Judge ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await judicialMessages.gotoNextStep('Send messages');
            await judicialMessages.sendMessageToAllocatedJudgeWithApplication();
            await judicialMessages.checkYourAnsAndSubmit();
            await judicialMessages.tabNavigation('Judicial messages');


            await judicialMessages.assertJudicialMessageHeaders();
            await expect(judicialMessages.page.getByText('Open', {exact: true})).toBeVisible();
            await expect(page.getByRole('cell', {
                name: formatToLongDate12hrTime(new Date()),
                exact: true
            }).locator('span')).toBeVisible();
            await expect(judicialMessages.page.getByText('To the allocated judge - Regard Hearing')).toBeVisible();

            await judicialMessages.expandMessageDetails('CTSC');
            await expect(page.locator('ccd-read-complex-field-collection-table')).toContainText('C2, 25 March 2021, 3:16pm');
            await expect(page.getByText('Allocated judge to decide on the hearing.')).toHaveCount(2);
            await expect(page.getByText('Same Day')).toBeVisible();

        });

    test('CTSC admin send message to Judge with document @xbrowser',
        async ({page, signInPage, judicialMessages}) => {
            casename = 'CTSC message Judge ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await judicialMessages.gotoNextStep('Send messages');
            await judicialMessages.sendMessageToAllocatedJudgeWithDocument();
            await judicialMessages.checkYourAnsAndSubmit();
            await judicialMessages.tabNavigation('Judicial messages');

            await expect(judicialMessages.page.getByText('To legal adviser - Regard Hearing assistance')).toBeVisible();
            await expect(judicialMessages.page.getByText('Open', {exact: true})).toBeVisible();
            await judicialMessages.expandMessageDetails('CTSC');
            await expect(judicialMessages.page.getByText(formatToLongDate12hrTime(new Date()))).toHaveCount(2);
            await expect(page.getByText('Hearing needs assistance from legal adviser.')).toHaveCount(2);
            await expect(page.getByText('Same Day')).toBeVisible();

        });

    test('Judge reply CTCS message @xbrowser ', async ({page, signInPage, judicialMessages}) => {
        casename = 'Judge Reply ' + dateTime.slice(0, 10);
        await updateCase(casename, caseNumber, caseDataJudgeMessage);
        await signInPage.visit();
        await signInPage.login(judgeUser.email, judgeUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Reply to messages');
        await judicialMessages.judgeReplyMessage();
        await judicialMessages.checkYourAnsAndSubmit();

        await judicialMessages.tabNavigation('Judicial messages');
        await judicialMessages.expandMessageDetails('Legal Adviser');
        await expect(page.getByText('Message history 1')).toBeVisible();
        await expect(page.getByText('Message history 2')).toBeVisible();
        await expect(page.getByText('Other Judge/Legal Adviser (judiciary-only@mailnesia.com)')).toHaveCount(2);
        await expect(page.getByText('Reply CTSC admin about the hearing.')).toHaveCount(2);

    });

    test.only('CTSC admin close the Message', async ({page, signInPage, judicialMessages}) => {
        casename = 'CTSC Admin Close Message ' + dateTime.slice(0, 10);
        await updateCase(casename, caseNumber, caseDataCloseMessage);
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await judicialMessages.gotoNextStep('Reply to messages');
        await judicialMessages.CTSCUserCloseMessage();
        await judicialMessages.checkYourAnsAndSubmit();

        await judicialMessages.tabNavigation('Judicial messages');
        await expect(page.getByText('Closed', {exact: true})).toBeVisible();
        await judicialMessages.expandMessageDetails('CTSC');
        await expect(page.getByText('Closing as message is actioned')).toBeVisible();
        await expect(page.getByText('Message history 1')).toBeVisible();
    })

});
