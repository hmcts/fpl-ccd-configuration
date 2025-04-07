import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class HearingUrgency extends BasePage {
    get hearingUrgencyHeading(): Locator {
        return this.page.getByRole('heading', { name: 'Hearing urgency' });
    }

    get hearingUrgency(): Locator {
        return this.page.getByRole('heading', { name: 'Hearing needed' });
    }

    get selectTypeOfHearing(): Locator {
        return this.page.getByLabel('Standard (between days 12-18)');
    }

    get areRespondentsAwareOfProceedings(): Locator {
        return this.page.getByRole('radio', { name: 'Yes' });
    }
  async hearingUrgencySmokeTest() {
    await expect(this.hearingUrgencyHeading).toBeVisible();
    await this.hearingUrgency.click();
    await expect(this.hearingUrgency).toBeVisible();
    await this.selectTypeOfHearing.click();
    await this.areRespondentsAwareOfProceedings.click();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
    await expect(this.page.getByText('has been updated with event:')).toBeVisible();

  }
}
