import {expect, type Locator} from "@playwright/test";
import {BasePage} from "./base-page";

export class ChangeCaseName extends BasePage {
    get changeCaseName(): Locator {
        return this.page.getByRole('heading', {name: 'Change case name', exact: true});
    }

    get caseName(): Locator {
        return this.page.getByLabel('Case name');
    }


    async updateCaseName() {
        await expect(this.changeCaseName).toBeVisible;
        await this.caseName.fill('CharDec24');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    };
}
