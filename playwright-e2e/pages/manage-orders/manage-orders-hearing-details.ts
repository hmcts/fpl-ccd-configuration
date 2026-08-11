import { Locator, Page } from "@playwright/test"
import { BasePage } from "../base-page";

export class ManageOrdersHearingDetails extends BasePage {
    readonly approvedHearingGroup: Locator;
    readonly approvedHearingYesRadioButton: Locator;
    readonly approvedHearingNoRadioButton: Locator;
    readonly applicationOnOrderGroup: Locator;
    readonly applicationOnOrderYesRadioButton: Locator;
    readonly applicationOnOrderNoRadioButton: Locator;


    constructor(page: Page) {
        super(page);
        this.approvedHearingGroup = page.getByRole('group', { name: 'Was the order approved at a hearing?' });
        this.approvedHearingYesRadioButton = this.approvedHearingGroup.getByRole('radio', { name: 'Yes' });
        this.approvedHearingNoRadioButton = this.approvedHearingGroup.getByRole('radio', { name: 'No' });
        this.applicationOnOrderGroup = page.getByRole('group', { name: 'Is there an application for the order on the system?' });
        this.applicationOnOrderYesRadioButton = this.applicationOnOrderGroup.getByRole('radio', { name: 'Yes' });
        this.applicationOnOrderNoRadioButton = this.applicationOnOrderGroup.getByRole('radio', { name: 'No' })
    }

    async checkApprovedHearingYes(): Promise<void> {
        await this.approvedHearingYesRadioButton.check();
    }

    async checkApprovedHearingNo(): Promise<void> {
        await this.approvedHearingNoRadioButton.check();
    }

    async checkApplicationOnOrderYes(): Promise<void> {
        await this.applicationOnOrderYesRadioButton.check();
    }

    async checkApplicationOnOrderNo(): Promise<void> {
        await this.applicationOnOrderNoRadioButton.check();
    }
}
