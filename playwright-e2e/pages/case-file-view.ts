import { Page } from "@playwright/test";
import { BasePage } from "./base-page";

export class CaseFileView extends BasePage {

    constructor(page: Page) {
        super(page);
    }

    async goToCFVTab() {
        await this.tabNavigation('Case File View');
    }

    async openFolder(name: string) {
        await this.currentPage.getByRole('button', { name: 'toggle ' + name }).click();
    }

    async moveDocument(fromFolder: string,toFolder:string ) {
        await this.openFolder(fromFolder);
        await this.currentPage.getByRole('button', { name: 'More document options', exact: true }).click();
        await this.currentPage.getByText('Change folder').click();
        await this.currentPage.getByLabel(toFolder, { exact: true }).check();
        await this.currentPage.getByRole('button', { name: 'Save', exact: true }).click();
    }

}
