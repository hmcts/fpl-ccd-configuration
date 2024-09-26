import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ReturnApplication extends BasePage {
  readonly returnApplication: Locator;
  readonly updateAndSubmit: Locator;
  readonly reasonForRejection: Locator;
  readonly needToChange: Locator;
  readonly submitApplication: Locator;
  readonly saveAndContinue: Locator;

  public constructor(page: Page) {
    super(page);
    this.returnApplication = page.getByLabel('Return application');
    this.updateAndSubmit = page.getByRole('button', { name: 'Go' });
    this.reasonForRejection = page.getByLabel('Application Incomplete');
    this.needToChange = page.getByLabel('Let the local authority know');
    this.submitApplication = page.getByRole('button', { name: 'Submit application' });
    this.saveAndContinue = page.getByRole('button', { name: 'Save and continue' });

  }

  async ReturnApplication() {
    await this.reasonForRejection.check();
    await this.needToChange.fill('test');
    await this.submitApplication.click();
    await this.saveAndContinue.click();
  }

  public async payForApplication() {
    await this.page.getByLabel('Payment by account (PBA) number').fill('PBA1234567');
    await this.page.getByLabel('Customer reference').fill('Customer reference');
    await this.checkYourAnsAndSubmit();
  }

  async SubmitApplication() {
    await this.submitApplication.check();
    await this.saveAndContinue.click();
    await this.checkYourAnsAndSubmit();
  }
}
