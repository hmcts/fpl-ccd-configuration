import { type Page, type Locator, expect } from "@playwright/test";
import {BasePage} from "./base-page";

export class GroundsForTheApplication extends BasePage {
    readonly page: Page;
    readonly groundsForTheApplicationHeading: Locator;
    readonly howDoesThisMeetTheThresholdCriteriaHeading: Locator;
    readonly notReceivingExpectedCareFromParentCheckBox: Locator;
    readonly detailsOfHowCaseMeetsThresholdCriteriaTextBox: Locator;
    // readonly continueButton: Locator;
    // readonly checkYourAnswersHeader: Locator;
    // readonly saveAndContinueButton: Locator;

    public constructor(page: Page) {
        super(page);
        this.page = page;
        this.groundsForTheApplicationHeading = page.getByRole('heading', { name: 'Grounds for the application' });
        this.howDoesThisMeetTheThresholdCriteriaHeading = page.getByRole('heading', { name: '*How does this case meet the threshold criteria?' });
        this.notReceivingExpectedCareFromParentCheckBox = page.getByLabel('Not receiving care that would be reasonably expected from a parent');
        this.detailsOfHowCaseMeetsThresholdCriteriaTextBox = page.getByRole("textbox", {name: '*Give details of how this case meets the threshold criteria (Optional)'});
        // this.continueButton = page.getByRole('button', { name: 'Continue' });
        // this.checkYourAnswersHeader = page.getByRole('heading', { name: 'Check your answers' });
        // this.saveAndContinueButton = page.getByRole('button', { name: 'Save and continue' });
    }

    async groundsForTheApplicationSmokeTest() {
        await expect (this.groundsForTheApplicationHeading).toBeVisible();
        await this.notReceivingExpectedCareFromParentCheckBox.click();
        await this.detailsOfHowCaseMeetsThresholdCriteriaTextBox.fill('Eum laudantium tempor, yet magni beatae. Architecto tempor. Quae adipisci, and labore, but voluptate, but est voluptas. Ipsum error minima. Suscipit eiusmod excepteur veniam. Consequat aliqua ex. Nostrud elit nostrum fugiat, yet esse nihil. Natus anim perspiciatis, and illum, so magni. Consequuntur eiusmod, so error. Anim magna. Dolores nequeporro, yet tempora. Amet rem aliquid.');
        await this.notReceivingExpectedCareFromParentCheckBox.isChecked();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
