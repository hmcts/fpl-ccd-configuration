import {expect, Page} from "@playwright/test";
import { BasePage } from "./base-page";

export class CaseFileView extends BasePage {
    docNewTab: Page;

    constructor(page: Page) {
        super(page);
        this.docNewTab = page;
    }

    async goToCFVTab() {
        await this.tabNavigation('Case File View');
    }

    async openFolder(name: string) {
        await this._page.getByRole('button', { name: 'toggle ' + name }).click();
    }

    async moveDocument(fromFolder: string,toFolder:string ) {
        await this.openFolder(fromFolder);
        await this._page.getByRole('button', { name: 'More document options', exact: true }).click();
        await this._page.getByText('Change folder').click();
        await this._page.getByLabel(toFolder, { exact: true }).check();
        await this._page.getByRole('button', { name: 'Save', exact: true }).click();
    }

    async openDocInNewTab() {
        await this.page.getByRole('button', {name: 'More document options', exact: true}).click();
        const docPagePromise = this.page.waitForEvent('popup');
        await this.page.getByText('Open in a new tab').click();
        this.docNewTab = await docPagePromise;
        await this.docNewTab.waitForLoadState();

    }

    async validatePDFContent(data: string) {

        await this.docNewTab.getByLabel('page number').fill('1');
        await this.docNewTab.getByLabel('page number').press('Enter');
        let numberOfPage = await this.docNewTab.getByRole('region').count();

        for (let i = 1; i < numberOfPage; i++) {
            await expect(this.docNewTab.getByText(`${data}`)).toBeVisible();
            await this.docNewTab.getByRole('button', {name: 'Next Page'}).click();
        }
    }
}

