import {test} from '../fixtures/create-fixture';
import {createCase, giveAccessToCase, updateCase} from "../utils/api-helper";
import {
    CTSCUser,
    newSwanseaLocalAuthorityUserOne,
    privateSolicitorOrgUser
} from "../settings/user-credentials";
import {expect} from "@playwright/test";
import {urlConfig} from "../settings/urls";
import caseWithChildrenCafcassSolicitorDemo
    from '../caseData/caseWithMultipleChildCafcassSolicitorDemo.json' assert {type: "json"};
import caseWithChildrenCafcassSolicitor
    from '../caseData/caseWithMultipleChildCafcassSolicitor.json' assert {type: "json"};
import caseWtihQuery from '../caseData/caseWithQuery.json' assert {type: "json"};

test.describe('Query management', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        expect(caseNumber).toBeDefined();
    });

    test('LA raise query',
        async ({
                   page, signInPage, queryManagement, caseFileView

               }) => {

            await test.step('Test data setup', async () => {
                caseName = 'LA raise a query ' + dateTime.slice(0, 10);
                if (urlConfig.env == 'demo') {
                    expect(await updateCase(caseName, caseNumber, caseWithChildrenCafcassSolicitorDemo)).toBeTruthy();
                } else {
                    expect(await updateCase(caseName, caseNumber, caseWithChildrenCafcassSolicitor)).toBeTruthy();
                }

                expect(await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[CHILDSOLICITORA]')).toBeTruthy();
            });


            await test.step('Test data setup', async () => {
                await signInPage.visit();
                await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
                await signInPage.navigateToCaseDetails(caseNumber);
            });

            await test.step('LA raise a new query', async () => {
                await queryManagement.gotoNextStep('Raise a new query');
                await queryManagement.assertQueryQuestion();
                await queryManagement.selectNewQuery();
                await queryManagement.clickContinue();
                await queryManagement.enterQueryDetails();
                await queryManagement.clickContinue();
                await queryManagement.clickSubmit();
                await expect(page.getByRole('heading', {name: 'Query submitted'})).toBeVisible();
                await expect(page.getByText('Your query has been sent to HMCTS')).toBeVisible();
                await expect.soft(page.getByText('Our team will read your query and respond.')).toBeVisible();
                await expect.soft(page.getByText('When the response is available it will be added to the \'Queries\' section')).toBeVisible();

            });
            await test.step('Assert LA can see Query details visible under query tab and attached query file in CFV', async () => {

                await queryManagement.goBackToCaseDetails();
                await queryManagement.tabNavigation('Queries');
                await queryManagement.assertQueryTable();
                await expect(page.getByRole('button', {name: 'Birth certificate format'})).toBeVisible();
                await expect(page.getByRole('cell', {name: '(local-authority)'}).first()).toBeVisible();
                await expect(page.getByRole('cell', {name: '(local-authority)'}).nth(1)).toBeVisible();
                await expect(page.getByRole('cell', {name: 'Awaiting Response'})).toBeVisible();
                await expect(page.getByRole('cell', {name: `${queryManagement.getCurrentDate()}`})).toBeVisible();

                await queryManagement.tabNavigation('Case File View');
                await caseFileView.openFolder('Uncategorised documents');
                await expect(caseFileView.page.getByText('testWordDoc.docx')).toBeVisible();
            });
            await test.step('Assert Query details are not visible under query tab to respondent solicitor', async () => {

                await queryManagement.clickSignOut();
                await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);

                await queryManagement.tabNavigation('Queries');
                await expect(page.getByRole('link', {name: 'Birth certificate format'})).toBeHidden();

                await queryManagement.tabNavigation('Case File View');
                await expect(queryManagement.page.getByText('Uncategorised documents')).toBeHidden();
            });
            await test.step('CTSC user respond to the query raised', async () => {

                await queryManagement.clickSignOut();
                await signInPage.login(CTSCUser.email, CTSCUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);
                await queryManagement.tabNavigation('Tasks');
                await queryManagement.waitForTask('Respond to a Query');
                await queryManagement.assignToMe();

                await queryManagement.respondToQuery(false);

                await expect(queryManagement.page.getByRole('heading', {name: 'Query response submitted'})).toBeVisible();
                await expect(queryManagement.page.getByText('This query response has been added to the case')).toBeVisible();
            })


            await test.step('Assert query response are visible under query tab', async () => {

                await queryManagement.backToCaseDetailsLink.click();
                await queryManagement.tabNavigation('Queries');
                await expect(queryManagement.page.getByText('Responded')).toBeVisible();
                await queryManagement.expandQuery('Birth certificate format');
                await expect(queryManagement.page.getByText('Response', {exact: true})).toBeVisible();
                await expect(queryManagement.page.getByText('Answered to the query raised by LA')).toBeVisible();
            });


        });
    test('LA raise follow up query',
        async ({
                   page, signInPage, queryManagement, caseFileView

               }) => {
            caseName = 'LA raise a follow up query ' + dateTime.slice(0, 10);

            expect(await updateCase(caseName, caseNumber, caseWtihQuery)).toBeTruthy();
            expect(await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[CHILDSOLICITORA]')).toBeTruthy();

            await test.step('Navigate to Test case', async () => {
                await signInPage.visit();
                await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
                await signInPage.navigateToCaseDetails(caseNumber);
            });
            await test.step('LA raise follow up query to CTSC', async () => {
                await queryManagement.tabNavigation('Queries');
                await queryManagement.askFollowupQuery();
                await queryManagement.clickContinue();
                await queryManagement.clickSubmit();

                await expect(page.getByRole('heading', {name: 'Query submitted'})).toBeVisible();
                await expect(page.getByText('Your query has been sent to HMCTS')).toBeVisible();
            });
            await test.step('Assert follow up query details  are visible under query tab', async () => {
                await queryManagement.goBackToCaseDetails();
                await queryManagement.tabNavigation('Queries');
                await expect(page.getByText('Awaiting Response')).toBeVisible();
            });
            await test.step('Assert follow up Query details are not visible under query tab to respondent solicitor', async () => {

                await queryManagement.clickSignOut();
                await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);

                await queryManagement.tabNavigation('Queries');
                await expect(page.getByRole('button', {name: 'Birth certificate format'})).toBeHidden();
            });
            await test.step('CTSC user respond to the follow up query raised', async () => {
                await queryManagement.clickSignOut();
                await signInPage.login(CTSCUser.email, CTSCUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);
                await queryManagement.tabNavigation('Tasks');
                await queryManagement.waitForTask('Respond to a Query');
                await queryManagement.assignToMe();
                await queryManagement.respondToQuery(true);
            });
            await test.step('Assert follow up query response are visible under query tab', async () => {
                await queryManagement.goBackToCaseDetails();
                await queryManagement.tabNavigation('Queries');

                await expect(page.getByText('Closed')).toBeVisible();
                await queryManagement.expandQuery('query by LA');
                //   await queryManagement.page.getByRole('button', {name: ''}).click();
                await expect(page.getByText('Response', {exact: true})).toHaveCount(2);
                await expect(page.getByText('Answered to the query raised by LA')).toBeVisible();
                await expect(queryManagement.page.getByText('This query has been closed by HMCTS staff.')).toBeVisible();
            })

        });

})
