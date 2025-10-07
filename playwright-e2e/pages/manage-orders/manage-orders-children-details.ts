import { Locator, Page } from '@playwright/test';
import { BasePage } from '../base-page';

export class ManageOrdersChildrenDetails extends BasePage {
    readonly orderAboutAllChildrenGroup: Locator;
    readonly orderAboutAllChildrenYesRadioButton: Locator;
    readonly orderAboutAllChildrenNoRadioButton: Locator;

    constructor(page: Page) {
        super(page);
        this.orderAboutAllChildrenGroup = page.getByRole('group', { name: 'Is the order about all the children?'});
        this.orderAboutAllChildrenYesRadioButton = this.orderAboutAllChildrenGroup.getByRole('radio', { name: 'Yes' });
        this.orderAboutAllChildrenNoRadioButton = this.orderAboutAllChildrenGroup.getByRole('radio', { name: 'No' });
    }

    async checkOrderAboutAllChildrenYes(): Promise<void> {
        await this.orderAboutAllChildrenYesRadioButton.check();
    }

    async checkOrderAboutAllChildrenNo(): Promise<void> {
        await this.orderAboutAllChildrenNoRadioButton.check();
    }
}
