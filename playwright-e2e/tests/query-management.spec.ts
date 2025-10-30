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
    });

    test('LA raise query',
        async ({
                   page, signInPage, queryManagement, caseFileView

               }) => {
            caseName = 'LA raise a query ' + dateTime.slice(0, 10);
            if (urlConfig.env == 'demo') {
                await updateCase(caseName, caseNumber, caseWithChildrenCafcassSolicitorDemo);
            } else {
                await updateCase(caseName, caseNumber, caseWithChildrenCafcassSolicitor);
            }

            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[CHILDSOLICITORA]');
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await queryManagement.gotoNextStep('Raise a new query');
            await expect.soft(queryManagement.page.getByText('Access issues or adding a new user')).toBeVisible();
            await expect.soft(queryManagement.page.getByText('Make a change of representation on a case (notice of change)')).toBeVisible();
            await expect.soft(queryManagement.page.getByText('I have a query in relation to a hearing')).toBeVisible();
            await expect.soft(queryManagement.page.getByText('Add counsel to a case')).toBeVisible();
            await expect.soft(queryManagement.page.getByText('Follow-up on an existing query')).toBeVisible();
            await expect.soft(queryManagement.page.getByLabel('Raise a new query')).toBeVisible();
            await queryManagement.selectNewQuery();
            await queryManagement.clickContinue();
            await queryManagement.enterQueryDetails();
            await queryManagement.clickContinue();
            await queryManagement.clickSubmit();

            await expect(page.getByRole('heading', {name: 'Query submitted'})).toBeVisible();
            await expect(page.getByText('Your query has been sent to HMCTS')).toBeVisible();
            await expect.soft(page.getByText('Our team will read your query and respond.')).toBeVisible();
            await expect.soft(page.getByText('When the response is available it will be added to the \'Queries\' section')).toBeVisible();

            await queryManagement.goBackToCaseDetails();
            await queryManagement.tabNavigation('Queries');

            await expect(page.getByRole('columnheader', {name: 'Query subject'})).toBeVisible();
            await expect(page.getByRole('columnheader', {name: 'Last submitted by'})).toBeVisible();
            await expect(page.getByRole('columnheader', {name: 'Last submission date'})).toBeVisible();
            await expect(page.getByRole('columnheader', {name: 'Last response date'})).toBeVisible();
            await expect(page.getByRole('columnheader', {name: 'Response status'})).toBeVisible();
            await expect(page.getByRole('link', {name: 'Birth certificate format'})).toBeVisible();
            await expect(page.getByRole('cell', {name: 'Local Authority '})).toBeVisible();
            await expect(page.getByRole('cell', {name: 'Awaiting Response'})).toBeVisible();
            await expect(page.getByRole('cell', {name: `${queryManagement.getCurrentDate()}`})).toBeVisible();

            await queryManagement.tabNavigation('Case File View');
            await caseFileView.openFolder('Uncategorised');
            await expect(caseFileView.page.getByText('testWordDoc.docx')).toBeVisible();

            await queryManagement.clickSignOut();
            await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await queryManagement.tabNavigation('Queries');
            await expect(page.getByRole('link', {name: 'Birth certificate format'})).toBeHidden();

            await queryManagement.tabNavigation('Case File View');
            await expect(queryManagement.page.getByText('Uncategorised')).toBeHidden();

            await queryManagement.clickSignOut();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await queryManagement.tabNavigation('Tasks');
            await queryManagement.waitForTask('Respond to a Query');
            await queryManagement.assignToMe();

            await queryManagement.respondToQuery(false);
            await queryManagement.tabNavigation('Queries');

            await expect(page.getByText('Responded')).toBeVisible();
            await queryManagement.page.getByRole('link', {name: 'Birth certificate format'}).click();
            await expect(page.getByText('Response', {exact: true})).toBeVisible();
            await expect(page.getByText('Answering to the query raised')).toBeVisible();
            await expect(page.getByRole('cell', {name: 'Closed'})).toBeVisible();

        });
    test('LA raise follow up query',
        async ({
                   page, signInPage, queryManagement, caseFileView

               }) => {
            caseName = 'LA raise a follow up query ' + dateTime.slice(0, 10);

            await updateCase(caseName, caseNumber, caseWtihQuery);
            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[CHILDSOLICITORA]');
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await queryManagement.tabNavigation('Queries');
            await queryManagement.askFollowupQuery();
            await queryManagement.clickContinue();
            await queryManagement.clickSubmit();

            await expect(page.getByRole('heading', {name: 'Query submitted'})).toBeVisible();
            await expect(page.getByText('Your query has been sent to HMCTS')).toBeVisible();

            await queryManagement.goBackToCaseDetails();
            await queryManagement.tabNavigation('Queries');
            await expect(page.getByText('Awaiting Response')).toBeVisible();

            await queryManagement.clickSignOut();
            await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await queryManagement.tabNavigation('Queries');
            await expect(page.getByRole('link', {name: 'Birth certificate format'})).toBeHidden();

            await queryManagement.clickSignOut();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await queryManagement.tabNavigation('Tasks');
            await queryManagement.waitForTask('Respond to a Query');
            await queryManagement.assignToMe();
            await page.pause();
            await queryManagement.respondToQuery(true);
            await queryManagement.tabNavigation('Queries');

            await expect(page.getByText('Responded')).toBeVisible();
            await queryManagement.page.getByRole('link', {name: 'Birth certificate format'}).click();
            await expect(page.getByText('Response', {exact: true})).toBeVisible();
            await expect(page.getByText('Answering to the query raised')).toBeVisible();
            await expect(page.getByRole('cell', {name: 'Closed'})).toBeVisible();

        });

})
