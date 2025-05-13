import {type Page} from "@playwright/test";
import {BasePage} from "./base-page";

export class ManageTTL extends BasePage {

    public constructor(page: Page) {
        super(page);

    }

    async overrideSystemTTL() {
        await this.page.getByRole('textbox', {name: 'Day'}).fill('5');
        await this.page.getByRole('textbox', {name: 'Month'}).fill((new Date().getMonth() + 2).toString());
        await this.page.getByRole('textbox', {name: 'Year'}).fill((new Date().getUTCFullYear() + 1).toString());

    }

    async suspendTTL() {
        await this.page.getByLabel('Yes').check();
    }
}
