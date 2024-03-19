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

}