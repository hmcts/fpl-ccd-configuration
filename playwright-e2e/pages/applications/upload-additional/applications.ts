import { BasePage } from "../../base-page";
import { expect, Locator, Page } from "@playwright/test";

export class Applications extends BasePage {
    readonly applyingForGroup: Locator;
    readonly otherSpecificOrderCheckbox: Locator;
    readonly c2OrderCheckbox: Locator;

    readonly typeOfC2ApplicationGroup: Locator;
    readonly applicationWithNoticeRadioButton: Locator;
    readonly applicationByConsentRadioButton: Locator;

    readonly confidentialApplicationGroup: Locator;
    readonly confidentialApplicationYesRadioButton: Locator;
    readonly confidentialApplicationNoRadioButton: Locator;

    readonly applicantDropdown: Locator;
    readonly applicantDropdownOptions: Locator;

    constructor(page: Page) {
        super(page);
        this.applyingForGroup = page.getByRole('group', { name:  'What application are you making?' });
        this.otherSpecificOrderCheckbox = this.applyingForGroup.getByRole('checkbox', { name: 'Other specific order' });
        this.c2OrderCheckbox = this.applyingForGroup.getByRole('checkbox', { name: 'C2 Application' });

        //this.typeOfC2ApplicationGroup = page.getByRole('group', { name: 'C2 application' });
        this.typeOfC2ApplicationGroup = page.getByRole('group', { name: 'C2 Application' });
        this.applicationWithNoticeRadioButton = this.typeOfC2ApplicationGroup.getByRole('radio', { name: 'Application with notice' });
        this.applicationByConsentRadioButton = this.typeOfC2ApplicationGroup.getByRole('radio', { name: 'Application by consent' });

        this.confidentialApplicationGroup = page.getByRole('group', { name: 'Is this a confidential application?' });
        this.confidentialApplicationYesRadioButton = this.confidentialApplicationGroup.getByRole('radio', { name: 'Yes' });
        this.confidentialApplicationNoRadioButton = this.confidentialApplicationGroup.getByRole('radio', { name: 'No' });

        this.applicantDropdown = page.locator('#applicantsList');
        this.applicantDropdownOptions = page.locator('#applicantsList > option');
    }

    async chekcOtherSpecificOrder(): Promise<void> {
        await this.otherSpecificOrderCheckbox.check();
    }

    async checkC2Order(): Promise<void> {
        // await this.c2OrderCheckbox.focus();
        // await this.c2OrderCheckbox.dblclick();
        await this. page.getByRole('checkbox', { name: 'C2 Application' }).check();
        await this.page.waitForTimeout(6000);
        await expect (this.page.getByText('Do you want to make the C2 application online or upload a paper form?')).toBeVisible();

    }

    async checkApplicationWithNotice(): Promise<void> {
        await this.applicationWithNoticeRadioButton.check();
    }

    async checkConfidentialApplicationYes(): Promise<void> {
        await this.confidentialApplicationYesRadioButton.check();
    }

    async selectApplicantValue(index: number): Promise<void> {
        const dropdownTotal: number = await this.applicantDropdownOptions.count();

        if (index < 0 || index >= dropdownTotal) {
            throw new Error(`Index exceeds numbers of items in dropdown.`);
        }

        const value: string | null = await this.applicantDropdownOptions.nth(index).getAttribute('value');
        if (!value) {
            throw new Error(`Option at index ${index} has no value.`);
        }

        await this.applicantDropdown.selectOption(value);
    }
}
