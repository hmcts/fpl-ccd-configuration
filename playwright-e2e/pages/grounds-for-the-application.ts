import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class GroundsForTheApplication extends BasePage{
    readonly groundsForTheApplicationHeading: Locator;
    readonly howDoesThisMeetTheThresholdCriteriaHeading: Locator;
    readonly notReceivingExpectedCareFromParentCheckBox: Locator;
    readonly detailsOfHowCaseMeetsThresholdCriteriaTextBox: Locator;
    readonly groundsForTheApplicationHasBeenUpdatedFinished: Locator;

    public constructor(page: Page) {
        super(page);
        this.groundsForTheApplicationHeading = page.getByRole('heading', { name: 'Grounds for the application' });
        this.howDoesThisMeetTheThresholdCriteriaHeading = page.getByRole('heading', { name: '*How does this case meet the threshold criteria?' });
        this.notReceivingExpectedCareFromParentCheckBox = page.getByLabel('Not receiving care that would be reasonably expected from a parent');
        this.detailsOfHowCaseMeetsThresholdCriteriaTextBox = page.getByRole("textbox", {name: '*Give details of how this case meets the threshold criteria (Optional)'});
        this.groundsForTheApplicationHasBeenUpdatedFinished = page.locator('xpath=//*[@id="taskListLabel"]/dt/ccd-markdown/div/markdown/div/p[4]/img',);
    }

    async groundsForTheApplicationSmokeTest() {
        await expect (this.groundsForTheApplicationHeading).toHaveText('Grounds for the application');
        await this.notReceivingExpectedCareFromParentCheckBox.click();
        await this.detailsOfHowCaseMeetsThresholdCriteriaTextBox.fill('Eum laudantium tempor, yet magni beatae. Architecto tempor. Quae adipisci, and labore, but voluptate, but est voluptas. Ipsum error minima. Suscipit eiusmod excepteur veniam. Consequat aliqua ex. Nostrud elit nostrum fugiat, yet esse nihil. Natus anim perspiciatis, and illum, so magni. Consequuntur eiusmod, so error. Anim magna. Dolores nequeporro, yet tempora. Amet rem aliquid.');
        await this.notReceivingExpectedCareFromParentCheckBox.isChecked();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
        await expect(this.groundsForTheApplicationHasBeenUpdatedFinished).toBeVisible();
    }
}
