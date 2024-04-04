import { type Page, type Locator, expect } from "@playwright/test";

export class OtherProceedings {
  readonly page: Page;
  readonly otherProceedingsHeading: Locator;
  readonly areThereAnyPastOrOngoingProccedingsReleventToCase: Locator;
  readonly continueButton: Locator;
  readonly saveAndContinueButton: Locator;
  readonly radioButton: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.otherProceedingsHeading = page.getByRole('heading', { name: 'Other Proceedings' });
    this.areThereAnyPastOrOngoingProccedingsReleventToCase = page.getByLabel('AreThereAnyPastOrOngoingProccedingsReleventToCase');
    this.continueButton = page.getByRole('button', { name: 'Continue' });
    this.saveAndContinueButton = page.getByRole('button', { name: 'Save and continue' });
  }
  async otherProceedingsSmokeTest() {
    await this.otherProceedingsHeading.isVisible();
    await this.page.getByRole('radio', { name: 'No', exact: true }).check();
    await this.page.getByRole('button', { name: 'Continue' }).click();
    await this.page.getByRole('button', { name: 'Save and continue' }).click();
  }
}
