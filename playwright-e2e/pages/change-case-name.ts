import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ChangeCaseName extends BasePage {
    readonly changeCaseName: Locator;
    readonly caseName: Locator;

    constructor(page: Page) {
        super(page);
        this.changeCaseName = page.getByRole('heading', { name: 'Change case name', exact: true });
        this.caseName = page.getByLabel('Case name');   
    }

    async updateCaseName() {
        await expect(this.changeCaseName).toBeVisible;
        await this.caseName.fill('CharDec24');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    };
};
