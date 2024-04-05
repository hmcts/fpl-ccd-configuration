import { type Page } from "@playwright/test";
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

    async approveUrgentDraftCMO() {
        await this.page.getByRole('radio', { name: 'Yes' }).check();
        await this.page.getByLabel('One or more of the orders contain a priority administrative action.').check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit(); 
    }
}