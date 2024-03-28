import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ApproveOrders extends BasePage {

    constructor(page: Page) {
        super(page);
    }

    async approveNonUrgentDraftCMO() {
       await this.page.getByRole('radio', { name: 'Yes' }).check();
       await this.clickContinue();
       await this.checkYourAnsAndSubmit();
    }
}