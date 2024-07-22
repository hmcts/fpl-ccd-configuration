import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ReturnApplication extends BasePage {
  readonly returnApplication: Locator;
  readonly updateAndSubmit: Locator;
  readonly reasonForRejection: Locator;
  readonly needToChange: Locator;
  readonly submitApplication: Locator;

  public constructor(page: Page) {
    super(page);
    this.returnApplication = page.getByLabel('Return application');
    this.updateAndSubmit = page.getByRole('button', { name: 'Go' });
    this.reasonForRejection = page.getByLabel('Application Incomplete');
    this.needToChange = page.getByLabel('Let the local authority know');
    this.submitApplication = page.getByRole('button', { name: 'Submit application' });

  }

  async ReturnApplication() {
    await this.returnApplication.check();
    await this.updateAndSubmit.check();
    await this.reasonForRejection.fill('test');
    await this.submitApplication.click();
  }

  public async payForApplication() {
    await this.page.getByLabel('Payment by account (PBA) number').fill('PBA1234567');
    await this.page.getByLabel('Customer reference').fill('Customer reference');
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }
}
