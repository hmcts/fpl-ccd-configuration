import {type Page, type Locator, expect} from "@playwright/test";
import {BasePage} from "./base-page";
import config from "../settings/test-docs/config";

export class QueryManagement extends BasePage {
    newQuery: Locator;
    querySubjectText: Locator;
    queryDetailText: Locator;
    relatedToHearingRadio: Locator;
    newDocument: Locator;
    documentCollection: Locator;
    respondDetail: Locator;
    addDocument: Locator;
    documentInput: Locator;
    backToCaseDetailsLink: Locator;


    public constructor(page: Page) {
        super(page);
        this.newQuery = page.getByLabel('Raise a new query');
        this.querySubjectText = page.getByLabel('Query subject');
        this.queryDetailText = page.getByLabel('Query detail');
        this.relatedToHearingRadio = page.getByRole('group', {name: 'Is the query hearing related?'});
        this.newDocument = page.getByRole('button', {name: 'Add new'});
        this.documentCollection = page.locator('#documentCollection_value');
        this.respondDetail = page.getByRole('textbox', {name: 'Response detail'});
        this.addDocument = page.getByRole('button', {name: 'Add new'});
        this.documentInput = page.locator('#documentCollection_value');
        this.backToCaseDetailsLink = page.getByRole('link', {name: 'Go back to the case'});


    }

    async selectNewQuery() {
        await this.newQuery.click();
    }

    async enterQueryDetails() {
        await this.querySubjectText.fill('Birth certificate format');

        await expect.soft(this.page.getByText('The subject should be a summary of your query')).toBeVisible();
        await expect.soft(this.page.getByText('Include as many details as possible so case workers can respond to your query')).toBeVisible();
        await this.queryDetailText.fill('Have birth certificate issued in aboard');
        await this.relatedToHearingRadio.getByLabel('Yes').click();
        await this.enterDate(new Date(new Date().setFullYear(new Date().getFullYear() + 1)));
        await this.newDocument.click();
        await expect(this.page.getByText('Only attach documents related to your query. For all other documents use your case management document upload function.')).toBeVisible();
        await this.documentCollection.setInputFiles(config.testWordFile);
        await this.waitForAllUploadsToBeCompleted();
    }

    async assignToMe() {
        await this.page.getByText('Assign to me').click();
    }

    async goBackToCaseDetails() {
        await this.backToCaseDetailsLink.click();
    }

    async askFollowupQuery() {
        await this.page.getByRole('link', {name: 'query by LA'}).click();
        await expect(this.page.getByText('Query details')).toBeVisible();
        await expect(this.page.getByText('Response', {exact: true})).toBeVisible();
        await this.page.getByRole('button', {name: 'Ask a follow-up question'}).click();
        await expect.soft(this.page.getByText('Response', {exact: true})).toBeVisible();
        await expect.soft(this.page.getByRole('cell', {name: 'Yes, the venue of the hearing'})).toBeVisible();
        await expect.soft(this.page.locator('ccd-query-details')).toContainText('Yes, the venue of the hearing on 12/5/2026, the center has access to wheelchairs chair access');
        await this.page.getByRole('textbox', {name: 'Query Body'}).fill('Needed translater  for German language for the next hearing');

    }

    async respondToQuery(closeTheQuery: boolean = false) {
        await this.page.getByRole('link', {name: 'Respond to a query'}).click();
        await this.respondDetail.fill('Answering to the query raised');
     //   await this.page.pause();
        await expect.soft(this.page.getByText('Closing this query means the parties can no longer send messages in this thread. ')).toBeVisible();
        await expect.soft(this.page.getByRole('checkbox', {name: 'I want to close this query'})).toBeVisible();

        if (closeTheQuery) {
            await this.page.getByRole('checkbox', { name: 'I want to close this query' }).check();
        }

        await expect.soft(this.page.getByText('Only attach documents related to your query. For all other documents use your case management document upload function.')).toBeVisible();
        await this.addDocument.click();
        await this.documentInput.setInputFiles(config.testWordFile);
        await this.clickContinue();
        await this.clickSubmit();
        await expect(this.page.getByRole('heading', {name: 'Query response submitted'})).toBeVisible();
        await expect(this.page.getByText('This query response has been added to the case')).toBeVisible();
        await this.backToCaseDetailsLink.click();

    }
}
