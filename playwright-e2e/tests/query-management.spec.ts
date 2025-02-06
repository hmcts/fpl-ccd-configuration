import { test } from '../fixtures/create-fixture';
import {createCase, giveAccessToCase, updateCase} from "../utils/api-helper";
import {
    CTSCUser,
    newSwanseaLocalAuthorityUserOne,
    privateSolicitorOrgUser
} from "../settings/user-credentials";
import { expect } from "@playwright/test";
import { testConfig } from "../settings/test-config";
import { setHighCourt } from '../utils/update-case-details';
import {urlConfig} from "../settings/urls";
import caseWithChildrenCafcassSolicitorDemo from "../caseData/caseWithMultipleChildCafcassSolicitorDemo.json";
import caseWithChildrenCafcassSolicitor from "../caseData/caseWithMultipleChildCafcassSolicitor.json";

test.describe('Query management', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test.only('LA raise query',
        async ({
                   page, signInPage, queryManagement,caseFileView

               }) => {
            caseName = 'LA raise a query ' + dateTime.slice(0, 10);
            if(urlConfig.env=='demo') {
                await updateCase(caseName, caseNumber, caseWithChildrenCafcassSolicitorDemo);
            }
            else{
                await updateCase(caseName, caseNumber, caseWithChildrenCafcassSolicitor);
            }
            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[CHILDSOLICITORA]');
            await signInPage.visit();
           await signInPage.login(newSwanseaLocalAuthorityUserOne.email,newSwanseaLocalAuthorityUserOne.password);
           await signInPage.navigateTOCaseDetails(caseNumber);
           await queryManagement.gotoNextStep('Raise a new query');
           await expect.soft(queryManagement.page.getByText('Access issues or adding a new user')).toBeVisible();
           await expect.soft(queryManagement.page.getByText('Make a change of representation on a case (notice of change)')).toBeVisible();
            await expect.soft(queryManagement.page.getByText('I have a query in relation to a hearing')).toBeVisible();
            await expect.soft(queryManagement.page.getByText('Add counsel to a case')).toBeVisible();
            await expect.soft(queryManagement.page.getByLabel('Raise a new query')).toBeVisible();
            await queryManagement.selectNewQuery();
            await queryManagement.clickContinue();
            await queryManagement.enterQueryDetails();
            await queryManagement.clickContinue();
            await queryManagement.clickSubmit();
            await expect(page.getByRole('heading', { name: 'Query submitted' })).toBeVisible();
            await expect(page.getByText('Your query has been sent to')).toBeVisible();
            await queryManagement.page.getByRole('link', { name: 'Go back to the case' }).click();
            await caseFileView.openFolder('Uncategorised');
            await expect(caseFileView.page.getByText('testPdf2.pdf')).toBeVisible();
            await queryManagement.tabNavigation('Queries');
             await expect(page.getByRole('table', { name: 'Local Authority' }).locator('div')).toBeVisible();
             await expect(page.getByRole('cell', { name: 'Birth certificate format' })).toBeVisible();
             await expect(page.getByRole('cell', { name: 'Awaiting Response' })).toBeVisible();
             await expect(page.getByRole('cell', { name: `${queryManagement.getCurrentDate()}` })).toBeVisible();
             await expect(page.getByRole('cell', { name: 'Local Authority' })).toBeVisible();


             // login in as respondent solicitor and assert the query is not visible

            await signInPage.signOut();
            await signInPage.


           await signInPage.page.pause();




            // await expect(page.getByRole('heading', { name: 'Select the type of query you' })).toBeVisible();
          //  await page.getByText('Access issues or adding a new').click();


           // await page.getByRole('button', { name: 'Continue' }).click();
           //
           //  await page.getByText('Previous Continue').click();
           //  await page.getByRole('button', { name: 'Continue' }).click();
           //  await page.getByRole('button', { name: 'Submit' }).click();
           //
           //
           //  await page.getByText('Queries').click();
            // await expect(page.getByRole('table', { name: 'Local Authority' }).locator('div')).toBeVisible();
            // await expect(page.getByRole('cell', { name: 'Query subject' })).toBeVisible();
            // await expect(page.getByRole('cell', { name: 'Awaiting Response' })).toBeVisible();
            // await expect(page.getByRole('cell', { name: 'Feb 2025' })).toBeVisible();
            // await expect(page.getByRole('cell', { name: 'Local Authority' })).toBeVisible();


            //Assertion
            // query details available under the query tab
            // CTSC user can see the WA task under the task list
            // query is not visible to other private professional users (respondent sol)

        })
    test('CTSC users respond query',
        async ({
                   page, signInPage, placement,
                   caseFileView
               }) => {
            caseName = 'CTSC user respond to query task ' + dateTime.slice(0, 10);
            // soft assert respond query event is not visible

            // assign the query to himself
            // respond to query

            //Assertion

            // query details available under the query tab
            // LA user able to see the response
            //other professional users cant have access to the

        })
})
