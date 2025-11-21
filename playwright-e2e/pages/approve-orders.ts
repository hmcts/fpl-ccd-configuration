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
        await this.page.pause()
        expect.soft(await this.page.getByText('If you click \'Continue\', the draft order will be approved without any changes. A coversheet will be added showing your name and the date of approval, and a court seal will also be applied to the final version.')).toBeVisible()
        expect.soft(await this.page.getByText('Please note that the preview shown below will not display the court seal, but it reflects the content that will be issued.')).toBeVisible();
        expect.soft(await this.page.getByText('If you wish to make any amendments, please return to the previous screen and select either \'No, I need to make changes\' or \'No, the applicant needs to make changes\'.')).toBeVisible();
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
