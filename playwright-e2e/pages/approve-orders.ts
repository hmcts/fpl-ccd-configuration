import { BasePage } from "./base-page";
import { expect, Locator, Page } from "@playwright/test";

export class ApproveOrders extends BasePage {
    readonly yesApproveOrder: Locator;
    readonly urgentOrder: Locator;

    public constructor(page: Page) {
        super(page);
        this.yesApproveOrder = page.getByRole('radio', { name: 'Yes' });
        this.urgentOrder = page.getByLabel('One or more of the orders');
    }

    async navigateToPageViaNextStep() {
        await this.gotoNextStep('Approve orders');
        await expect(this.page.getByRole('group', { name: 'Is this order ready to be sealed and issued' }).locator('span')).toBeVisible();
    }

    async approveOrders() {
        await this.yesApproveOrder.click();
        await this.clickContinue();
        await this.urgentOrder.check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
        await expect(this.page.getByText('has been updated with event: Approve orders')).toBeVisible();
    }
  
      async approveNonUrgentDraftCMO() {
       await this.yesApproveOrder.click();
       await this.clickContinue();
       await this.clickContinue();
       await this.checkYourAnsAndSubmit();
       await expect(this.page.getByText('has been updated with event: Approve orders')).toBeVisible();
    }
}
