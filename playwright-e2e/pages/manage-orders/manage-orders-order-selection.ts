import {Locator, Page} from "@playwright/test"
import { BasePage } from "../base-page";

export class ManageOrdersOrderSelection extends BasePage {
    readonly orderChoiceGroup: Locator;
    readonly c43RadioButton: Locator;

    constructor(page: Page) {
        super(page);
        this.orderChoiceGroup = page.getByRole('group', { name: 'Select order' });
        this.c43RadioButton = this.orderChoiceGroup.getByRole('radio', { name: 'Child arrangements, Specific issue, Prohibited steps (C43)'});
    }

    async checkC43RadioButton(): Promise<void> {
        await this.c43RadioButton.check();
    }
}
