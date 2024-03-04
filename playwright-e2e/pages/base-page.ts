import { type Page, type Locator, expect } from "@playwright/test";

export abstract class BasePage {

  readonly nextStep: Locator;
  readonly go: Locator;
  readonly page: Page;
  readonly saveAndContinue: Locator;
  readonly continue: Locator;
  readonly submit: Locator;


  constructor(page: Page) {
    this.page = page;
    this.nextStep = page.getByLabel('Next step');
    this.go = page.getByRole('button', { name: 'Go' });
    this.saveAndContinue = page.getByRole('button', { name: 'Save and continue' });
    this.continue = page.getByRole('button', { name: 'Continue' });
    this.submit = page.getByRole('button', { name: 'Submit' });
  }
  async gotoNextStep(eventName: string) {
    await this.nextStep.selectOption(eventName);
    await this.go.click();
  }
  async checkYourAnsAndSubmit() {
    await this.saveAndContinue.click();
  }
  async clickSubmit() {
    await this.submit.click();
  }

  async tabNavigation(tabName: string) {
    await this.page.getByRole('tab', { name: tabName }).click();
  }
  async clickContinue() {
    await this.continue.click();
  }

  async waitForAllUploadsToBeCompleted() {
    let locs = await this.page.getByText('Cancel upload').all();
    for (let i = 0; i < locs.length; i++) {
      await expect(locs[i]).toBeDisabled();
    }
  }


}


