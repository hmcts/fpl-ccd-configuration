import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class HearingUrgency extends BasePage {
  readonly hearingUrgencyHeading: Locator;
  readonly standardHearingRadioButton: Locator;
  readonly areRespondentsAwareOfProceedings: Locator;
  readonly continue: Locator;

  public constructor(page: Page) {
    super(page);
    this.hearingUrgencyHeading = page.getByRole('heading', { name: 'Hearing urgency' });
    this.standardHearingRadioButton = page.getByRole('radio', { name: 'Standard (between days 12-18)' });
    this.areRespondentsAwareOfProceedings = page.getByRole('radio', { name: 'Yes' });
    this.continue = page.getByRole('button', { name: 'Continue' });
  }

  async hearingUrgency(): Promise<void> {
      await this.standardHearingRadioButton.click();
      await this.areRespondentsAwareOfProceedings.click();
      await Promise.all([
          this.page.waitForResponse((response) =>
              response.url().includes('validate?pageId=hearingNeeded1') &&
              response.status() === 200
          ),
          await this.continue.click(),

          this.page.waitForResponse((response) =>
              response.url().includes('api/wa-supported-jurisdiction/get') &&
              response.status() === 200
          ),
          await this.checkYourAnsAndSubmit()
      ]);

  }

  async hearingUrgencySmokeTest() {
    await expect(this.hearingUrgencyHeading).toBeVisible();
    await this.standardHearingRadioButton.click();
    await this.areRespondentsAwareOfProceedings.click();
    await this.continue.click();
      await Promise.all([
          this.page.waitForResponse(response => {
              const url = response.url();
              return (
                  url.includes('/api/wa-supported-jurisdiction/get') &&
                  response.request().method() === 'GET' &&
                  response.status() === 200
              );
          }),
          await this.checkYourAnsAndSubmit()
      ]);
    await expect(this.page.getByText('has been updated with event:')).toBeVisible();

  }
}
