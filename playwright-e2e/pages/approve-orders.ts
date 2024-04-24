import { BasePage } from "./base-page";
import { expect, Locator, Page } from "@playwright/test";

export class ApproveOrders extends BasePage {
    readonly yesApproveOrder: Locator;

    public constructor(page: Page) {
        super(page);
        this.yesApproveOrder = page.getByText("Yes")
    }

    async navigateToPageViaNextStep() {
        await this.gotoNextStep('Approve orders');
        await expect(this.page.getByText('Is this order ready to be sealed and issued? (Optional)')).toBeVisible();
    }

    async approveOrders() {
        await this.yesApproveOrder.click();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
        await expect(this.page.getByText('has been updated with event: Approve orders')).toBeVisible();
    }
}
