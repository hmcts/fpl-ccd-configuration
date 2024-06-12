import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class OtherProceedings extends BasePage {

  readonly otherProceedingsHeading: Locator;
  readonly areThereAnyPastOrOngoingProccedingsReleventToCase: Locator;

  public constructor(page: Page) {
    super(page);
    this.otherProceedingsHeading = page.getByRole('heading', { name: 'Other Proceedings' });
    this.areThereAnyPastOrOngoingProccedingsReleventToCase = page.getByRole('radio', { name: 'No', exact: true });
  }

  async otherProceedingsSmokeTest() {
    await this.otherProceedingsHeading.isVisible();
    await this.areThereAnyPastOrOngoingProccedingsReleventToCase.check();
    await this.clickContinue();
    await expect(this.checkYourAnswersHeader).toBeVisible();
    await this.checkYourAnsAndSubmit();
  }
}
