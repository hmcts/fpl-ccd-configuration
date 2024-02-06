import { test} from '../fixtures/create-fixture';
import {Apihelp} from '../utils/apiFixture';
import {UrlConfig} from "../settings/urls";
import caseData from '../caseData/mandatorySubmissionFields.json';
import { newSwanseaLocalAuthorityUserOne,CTSCUser } from '../settings/userCredentials';
import { expect } from '@playwright/test';


test.describe('send and reply message',()=>{
  let apiDataSetup = new Apihelp();
  const dateTime = new Date().toISOString();
  let caseNumber : string;
  test.beforeAll(()  => {

  });

  test('CTSC admin send message to Judge',
    async ({page,signInPage}) => {
    // await  apiDataSetup.createCase('CTSCSendMessageJudge');
   // await apiDataSetup.updateCase(caseData);
   // console.log (await  apiDataSetup.gettestDocDetails());
   test.step('Set up server s',async()=>{

   caseNumber =  await apiDataSetup.createCase('e2e case',newSwanseaLocalAuthorityUserOne);
   console.log(caseNumber);
   let casename = 'Judge Test' + dateTime.slice(0, 10);
   await apiDataSetup.updateCase(casename,caseNumber,caseData);
   console.log(`${UrlConfig.aatUrl}case-details/${caseNumber}`);
   });

   test.step('Navigate to case details',async()=>{
    await  signInPage.visit();
    await signInPage.login(CTSCUser.email,CTSCUser.password);
    await  signInPage.navigateTOCaseDetails(`${UrlConfig.aatUrl}case-details/${caseNumber}`);

   });
   test.step('Enter Message Details',async()=>{

     await page.getByRole('group',{name: 'Is it about an Application?'}).getByLabel('Yes').check();
     await page.getByLabel('Which application?').selectOption('C2, 25 March 2021, 3:16pm');
     await page.getByLabel('Sender', { exact: true }).selectOption('CTSC');
     await page.getByLabel('Recipient', { exact: true }).selectOption('Allocated Judge');
     await page.getByLabel('Recipient\'s email address').click();
     await page.getByLabel('Recipient\'s email address').fill('Judge@email.com');
     await page.getByLabel('Message subject').click();
     await page.getByLabel('Message subject').fill('Message To the allocated Judge');
     await page.getByLabel('Urgency (Optional)').click();
     await page.getByLabel('Urgency (Optional)').fill('Urgent');
     await page.getByRole('button', { name: 'Continue' }).click();
     await page.getByLabel('Message').click();
     await page.getByLabel('Message').fill('Message send to Allocated Judge');
     await page.getByRole('button', { name: 'Continue' }).click();
     await page.getByRole('button', { name: 'Save and continue' }).click();
   });
  test.step('Assert Judicial Message',async()=>{
    await page.getByText('Judicial messages').click();
    await expect(page.getByText('FamilyPublicLaw+ctsc@gmail.com - Message send to Allocated Judge')).toBeVisible();

  });






    });

});







