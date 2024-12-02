import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ChangeCaseName extends BasePage {
    readonly changeCaseName: Locator;
    readonly caseName: Locator;
    readonly continueButton: Locator;
    readonly saveAndContinue: Locator;

    constructor(page: Page) {
        super(page);
        this.changeCaseName = page.getByRole('heading', { name: 'Change case name', exact: true });
        this.caseName = page.getByLabel('Case name');
        this.continueButton = page.getByRole('button', { name: 'Continue' });
        this.saveAndContinue = page. getByRole('button', { name: 'Save and continue' });
    }

    async updateCaseName() {
        await expect(this.changeCaseName).toBeVisible;
        await this.caseName.fill('CharDec24');
        await this.continueButton.click();
        await this.saveAndContinue.click();
    };
};
