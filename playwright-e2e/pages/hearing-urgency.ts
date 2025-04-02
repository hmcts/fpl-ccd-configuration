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

  // private readonly _hearingUrgencyHeading: Locator;
  // private readonly _hearingUrgency: Locator;
  // private readonly _selectTypeOfHearing: Locator;
  // private readonly _areRespondentsAwareOfProceedings: Locator;
  // private readonly _continue: Locator;

  // public constructor(page: Page) {
  //   super(page);
  //   this._hearingUrgencyHeading =
  //   this._hearingUrgency =
  //   this._selectTypeOfHearing =
  //   this._areRespondentsAwareOfProceedings =
  //   this._continue =
  //
  // }
  async hearingUrgencySmokeTest() {
    await expect(this.hearingUrgencyHeading).toBeVisible();
    await this.hearingUrgency.click();
    await expect(this.hearingUrgency).toBeVisible();
    await this.selectTypeOfHearing.click();
    await this.areRespondentsAwareOfProceedings.click();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }
}
