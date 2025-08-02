import { Locator, Page } from '@playwright/test'
import { BasePage } from '../base-page';

export class ManageOrdersReview extends BasePage {
    readonly orderPdfLabel: Locator;
    readonly orderCloseCaseGroup: Locator;
    readonly closeCaseYesRadioButton: Locator;
    readonly closeCaseNoRadioButton: Locator;

    constructor(page: Page) {
        super(page);
        this.orderPdfLabel = page.getByRole('link', { name: /\.pdf$/i });
        this.orderCloseCaseGroup = page.getByRole('group', { name: 'Does this order close the case?' });
        this.closeCaseYesRadioButton = this.orderCloseCaseGroup.getByRole('radio', { name: 'Yes' });
        this.closeCaseNoRadioButton = this.orderCloseCaseGroup.getByRole('radio', { name: 'No' });
    }

    async checkCloseCaseYes(): Promise<void> {
        await this.closeCaseYesRadioButton.check();
    }

    async checkCloseCaseNo(): Promise<void> {
        await this.closeCaseNoRadioButton.check();
    }
}
