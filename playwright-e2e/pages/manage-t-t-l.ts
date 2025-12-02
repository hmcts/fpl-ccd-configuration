import {type Page} from "@playwright/test";
import {BasePage} from "./base-page";
import {addMonthsToDate} from "../utils/util-helper";

export class ManageTTL extends BasePage {

    public constructor(page: Page) {
        super(page);

    }

    async overrideSystemTTL() {
        const futureDate = addMonthsToDate(new Date(), 15);
        await this.page.getByRole('textbox', {name: 'Day'}).fill(futureDate.getUTCDate().toString());
        await this.page.getByRole('textbox', {name: 'Month'}).fill(futureDate.getUTCMonth().toString());
        await this.page.getByRole('textbox', {name: 'Year'}).fill(futureDate.getUTCFullYear().toString());
        await this.page.press('body', 'Tab');

    }

    async suspendTTL() {
        await this.page.getByLabel('Yes').check();
    }
}
