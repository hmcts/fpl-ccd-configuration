import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class HearingUrgency extends BasePage {
  readonly hearingUrgencyHeading: Locator;
  readonly hearingUrgency: Locator;
  readonly selectTypeOfHearing: Locator;
  readonly areRespondentsAwareOfProceedings: Locator;
  readonly continue: Locator;

  public constructor(page: Page) {
    super(page);
    this.hearingUrgencyHeading = page.getByRole('heading', { name: 'Hearing urgency' });
    this.hearingUrgency = page.getByRole('heading', { name: 'Hearing needed' });
    this.selectTypeOfHearing = page.getByLabel('Standard (between days 12-18)');
    this.areRespondentsAwareOfProceedings = page.getByRole('radio', { name: 'Yes' });
    this.continue = page.getByRole('button', { name: 'Continue' });

  }
  async hearingUrgencySmokeTest() {
    await expect(this.hearingUrgencyHeading).toBeVisible();
    await this.hearingUrgency.click();
    await expect(this.hearingUrgency).toBeVisible();
    await this.selectTypeOfHearing.click();
    await this.areRespondentsAwareOfProceedings.click();
    await this.continue.click();
    await this.checkYourAnsAndSubmit();
    await expect(this.page.getByText('has been updated with event:')).toBeVisible();

  }
}
