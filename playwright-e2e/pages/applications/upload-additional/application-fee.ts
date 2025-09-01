import { BasePage } from "../../base-page";
import { Locator, Page } from "@playwright/test";

export class ApplicationFee extends BasePage {
    readonly paidWithPBAGroup: Locator;
    readonly paidWithPBAYesRadioButton: Locator;
    readonly paidWithPBANoRadioButton: Locator

    constructor(page: Page) {
        super(page);
        this.paidWithPBAGroup = page.getByRole('group', { name: 'Do you want to enter PBA details?' });
        this.paidWithPBAYesRadioButton = this.paidWithPBAGroup.getByRole('radio', { name: 'Yes' });
        this.paidWithPBANoRadioButton = this.paidWithPBAGroup.getByRole('radio', { name: 'No' });
    }

    async checkPaidWithPBAYes(): Promise<void> {
        await this.paidWithPBAYesRadioButton.check();
    }

    async checkPaidWithPBANo(): Promise<void> {
        await this.paidWithPBANoRadioButton.check();
    }
}
