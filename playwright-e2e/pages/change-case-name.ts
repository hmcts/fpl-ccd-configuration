import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ChangeCaseName extends BasePage {
    get changeCaseName(): Locator {
        return this.page.getByRole('heading', { name: 'Change case name', exact: true });
    }

    get caseName(): Locator {
        return this.page.getByLabel('Case name');
    }


    // constructor(page: Page) {
    //     super(page);
    //     this._changeCaseName =
    //     this._caseName =
    // }

    async updateCaseName() {
        await expect(this.changeCaseName).toBeVisible;
        await this.caseName.fill('CharDec24');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    };
};
