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
        // Month is zero-based, so we add 1 to get the correct month number
        await this.page.getByRole('textbox', {name: 'Month'}).fill((futureDate.getUTCMonth()+1).toString());
        await this.page.getByRole('textbox', {name: 'Year'}).fill(futureDate.getUTCFullYear().toString());
        await this.page.press('body', 'Tab');

    }

    async suspendTTL() {
        await this.page.getByLabel('Yes').check();
    }
}
