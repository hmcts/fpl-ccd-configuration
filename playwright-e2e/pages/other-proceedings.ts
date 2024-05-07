import { type Page, type Locator, expect } from "@playwright/test";

export class OtherProceedings {
  readonly page: Page;
  readonly otherProceedingsHeading: Locator;
  readonly areThereAnyPastOrOngoingProccedingsReleventToCase: Locator;
  readonly continueButton: Locator;
  readonly saveAndContinueButton: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.otherProceedingsHeading = page.getByRole('heading', { name: 'Other Proceedings' });
    this.areThereAnyPastOrOngoingProccedingsReleventToCase = page.getByRole('radio',  { name: 'No', exact: true });
    this.continueButton = page.getByRole('button', { name: 'Continue' });
    this.saveAndContinueButton = page.getByRole('button', { name: 'Save and continue' });
       }
    
  async otherProceedingsSmokeTest() {
    await this.otherProceedingsHeading.isVisible();
    await this.areThereAnyPastOrOngoingProccedingsReleventToCase.check();
    await this.continueButton.click();
    await this.saveAndContinueButton.click();
  }
}
