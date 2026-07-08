import {Locator, Page} from "@playwright/test"
import { BasePage } from "../base-page";

export class ManageOrdersManageOrdersOperations extends BasePage {
    readonly orderChoiceGroup: Locator;
    readonly createOrderRadioButton: Locator;
    readonly uploadOrderRadioButton: Locator;
    readonly amendOrderRadioButton: Locator;

    constructor(page: Page) {
        super(page);
        this.orderChoiceGroup = page.getByRole('group', { name: 'What do you want to do?' });
        this.createOrderRadioButton = this.orderChoiceGroup.getByRole('radio', { name: 'Create an order' });
        this.uploadOrderRadioButton = this.orderChoiceGroup.getByRole('radio', { name: 'Upload an order' });
        this.amendOrderRadioButton = this.orderChoiceGroup.getByRole('radio', { name: 'Amend order under the slip rule' });
    }

    async checkCreateAnOrder(): Promise<void> {
        await this.createOrderRadioButton.check();
    }

    async checkUploadOrder(): Promise<void> {
        await this.uploadOrderRadioButton.check();
    }

    async checkAmendOrder(): Promise<void> {
        await this.amendOrderRadioButton.check();
    }
}
