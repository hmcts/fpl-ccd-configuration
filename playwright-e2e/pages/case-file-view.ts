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

        await this.page.getByRole('button', { name: 'toggle ' + name }).click();
    }

    async moveDocument(fromFolder: string,toFolder:string ) {
        await this.openFolder(fromFolder);
        await this.page.getByRole('button', { name: 'More document options', exact: true }).click();
        await this.page.getByText('Change folder').click();
        await this.page.getByLabel(toFolder, { exact: true }).check();
        await this.page.getByRole('button', { name: 'Save', exact: true }).click();
    }

}
