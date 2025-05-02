import {test} from '../fixtures/create-fixture';
import caseData from "../caseData/mandatorySubmissionFields.json" assert {type: "json"};
import { newSwanseaLocalAuthorityUserOne} from '../settings/user-credentials';
import {expect} from '@playwright/test';
import {createCase, updateCase} from "../utils/api-helper";


test.describe('Manage case linking @flaky', () => {
    test.setTimeout(600_000);
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;
    let linkedCase1: string;
    let linkedCase2: string;
    let linkedCase3: string;
    let updatedlinkedCase: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        linkedCase1 = await createCase('linkedCase1', newSwanseaLocalAuthorityUserOne);
        linkedCase2 = await createCase('linkedCase2', newSwanseaLocalAuthorityUserOne);
        //  linkedCase3  = await createCase('linkedCase3',newSwanseaLocalAuthorityUserOne);
        await updateCase('linkedCase1', linkedCase1, caseData);
        await updateCase('linkedCase2', linkedCase2, caseData);
        //  await updateCase('linkedCase3',linkedCase3,caseData);
    });

    test('CTSC user  link cases ',
        async ({page, ctscUser, caseLink}) => {
            test.slow();

            casename = 'CTSC admin link cases ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseData);
            await caseLink.switchUser(ctscUser.page);
            await caseLink.navigateTOCaseDetails(caseNumber);
            await caseLink.gotoCaseLinkNextStep('Link cases');
            await expect.soft(page.getByRole('heading', {name: 'Before you start'})).toBeVisible();
            await expect(page.getByText('If the cases to be linked has no lead, you can start the linking journey from any of those cases.',{exact:true})).toBeVisible();
            await expect(page.getByText('If a group of linked cases has a lead case, you must start from the lead case.',{exact:true})).toBeVisible();
            await caseLink.clickSubmit();
            await caseLink.proposeCaseLink(linkedCase1, ['Related proceedings', 'Same Party', 'Same child/ren']);
            await expect(caseLink.page.getByText(caseLink.hypenateCaseNumber(linkedCase1))).toBeVisible();
            await caseLink.proposeCaseLink(linkedCase2, ['Same Party']);
            await expect(caseLink.page.getByText(caseLink.hypenateCaseNumber(linkedCase2))).toBeVisible();
            await caseLink.clickNext();
            await expect.soft(caseLink.page.getByText('Proposed case links')).toBeVisible();
            await caseLink.clickSubmit();
            await caseLink.tabNavigation("Linked Cases");
            await caseLink.reloadAndCheckForText('linkedCase1');

            await expect.soft(caseLink.page.getByText('This case is linked to')).toBeVisible();
            await expect.soft(caseLink.page.locator('ccd-linked-cases-to-table').getByRole('columnheader', {name: 'Case name and number'})).toBeVisible();
            await expect.soft(caseLink.page.locator('ccd-linked-cases-to-table').getByRole('columnheader', {name: 'Case type'})).toBeVisible();
            await expect.soft(caseLink.page.locator('ccd-linked-cases-to-table').getByRole('columnheader', {name: 'Service'})).toBeVisible();
            await expect.soft(caseLink.page.locator('ccd-linked-cases-to-table').getByRole('columnheader', {name: 'State'})).toBeVisible();
            await expect.soft(caseLink.page.locator('ccd-linked-cases-to-table').getByRole('columnheader', {name: 'Reasons for case link'})).toBeVisible();
            await expect(caseLink.page.getByRole('link', {name: 'linkedCase1 ' + caseLink.hypenateCaseNumber(linkedCase1)})).toBeVisible();
            await expect(caseLink.page.getByRole('link', {name: 'linkedCase2 ' + caseLink.hypenateCaseNumber(linkedCase2)})).toBeVisible();
            await caseLink.openLinkedCase(caseLink.hypenateCaseNumber(linkedCase1));
            await expect(caseLink.linkedCasePage.getByRole('link', {name: 'e2e case ' + caseLink.hypenateCaseNumber(caseNumber)})).toBeVisible();

            await caseLink.gotoNextStep('Manage case links');
            await expect.soft(caseLink.page.getByRole('heading', {name: 'Before you start'})).toBeVisible();
            await expect.soft(caseLink.page.getByText('If there are linked hearings for the case you need to un-link then you must unlink the hearing first.')).toBeVisible();
            await caseLink.clickNext();
            await expect.soft(caseLink.page.getByText('Select the cases you want to unlink from this case')).toBeVisible();
            await caseLink.selectCaseToUnlink(linkedCase1);
            await caseLink.clickSubmit();
            await caseLink.clickSubmit();

            // assert
            await caseLink.tabNavigation("Linked Cases");
            await caseLink.reloadAndCheckForText('linkedCase2');
            await expect(caseLink.page.getByRole('link', {name: 'linkedCase1 ' + caseLink.hypenateCaseNumber(linkedCase1)})).toBeHidden();

        });

});

