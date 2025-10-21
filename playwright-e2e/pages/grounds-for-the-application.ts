import { type Page, type Locator, expect } from "@playwright/test";
import { fill } from "lodash";

export class GroundsForTheApplication {
    readonly page: Page;
    readonly groundsForTheApplicationHeading1: Locator;
    readonly groundsForTheApplicationHeading2: Locator;
    readonly howDoesThisMeetTheThresholdCriteriaHeading: Locator;
    readonly childBeyondParentalControlCheckbox: Locator;
    readonly reasonBehindChildSuffering: Locator;
    readonly notReceivingExpectedCareFromParentCheckBox: Locator;
    readonly thresholdDocument: Locator;
    readonly noProvieSummaryRadioButton: Locator;
    readonly continueButton: Locator;
    readonly checkYourAnswersHeader: Locator;
    readonly saveAndContinueButton: Locator;
    readonly provideSummary: Locator;

    public constructor(page: Page) {
        this.page = page;
        this.groundsForTheApplicationHeading1 = page.getByRole('heading', { name: 'Grounds for the application', level: 1 });
        this.groundsForTheApplicationHeading2 = page.getByRole('heading', { name: 'Grounds for the application', level: 2 });
        this.howDoesThisMeetTheThresholdCriteriaHeading = page.getByRole('heading', { name: '*How does this case meet the threshold criteria?' });
        this.reasonBehindChildSuffering = page.getByText('What is the reason behind the child suffering or being likely to suffer significant harm?');
        this.childBeyondParentalControlCheckbox = page.getByRole('checkbox', { name: 'Child is beyond parental' });
        this.notReceivingExpectedCareFromParentCheckBox = page.getByLabel('Not receiving care that would be reasonably expected from a parent');
        this.thresholdDocument = page.getByText('Do you have the threshold document?');
        this.noProvieSummaryRadioButton = page.getByLabel('No, I will provide a summary');
        this.provideSummary = page.getByLabel('Provide a summary of how this');
        this.continueButton = page.getByRole('button', { name: 'Continue' });
        this.checkYourAnswersHeader = page.getByRole('heading', { name: 'Check your answers' });
        this.saveAndContinueButton = page.getByRole('button', { name: 'Save and continue' });
     }

     async checkChildBeyondParentalControl(): Promise<void> {
        await this.childBeyondParentalControlCheckbox.check();
     }

     async checkNoProvideSummaryRadioButton(): Promise<void> {
        await this.noProvieSummaryRadioButton.check();
     }

     async fillSummary(text: string): Promise<void> {
        await this.provideSummary.fill(text);
     }


    async groundsForTheApplicationSmokeTest() {
        await expect(this.groundsForTheApplicationHeading1).toBeVisible();
        await expect(this.groundsForTheApplicationHeading2).toBeVisible();
        await expect(this.reasonBehindChildSuffering).toBeVisible();
        await this.childBeyondParentalControlCheckbox.check({ force: true });
        await expect(this.thresholdDocument).toBeVisible
        await this.noProvieSummaryRadioButton.check({ force: true });
        await this.provideSummary.fill('Test');
        await this.continueButton.click();
        await expect(this.checkYourAnswersHeader).toBeVisible();
        await this.saveAndContinueButton.click();
        await expect(this.page.getByText('has been updated with event:')).toBeVisible();
     }
}
