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
    backToCaseDetails: Locator;


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
        this.backToCaseDetails = page.getByRole('link', {name: 'Go back to the case'});


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

    async respondToQuery(closeTheQuery: boolean = true) {

        await this.page.getByRole('link', {name: 'Respond to a query'}).click();
        // await expect(page.getByRole('caption').getByText('Query details')).toBeVisible();
        await this.respondDetail.fill('Answering to the query raised');
        await this.addDocument.click();
        await expect.soft(this.page.getByRole('checkbox', { name: 'I want to close this query' })).toBeVisible();

if (closeTheQuery) {
    //
    await this.page.getByRole('checkbox', {name: 'I want to close this query'}).check();
}
         await expect.soft(this.page.getByText('Closing the query means the parties can no longer send message in this thread.')).toBeVisible();



        await expect.soft(this.page.getByText('Only attach documents related')).toBeVisible();
        await this.documentInput.setInputFiles(config.testWordFile);
        await this.clickContinue();
        await this.clickSubmit();
        await expect(this.page.getByRole('heading', {name: 'Query response submitted'})).toBeVisible();
        await expect(this.page.getByText('This query response has been added to the case')).toBeVisible();


        await this.backToCaseDetails.click();

    }
}
