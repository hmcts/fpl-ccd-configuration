import { type Page, type Locator, expect } from "@playwright/test";
export class BasePage {

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
  readonly continueButton: Locator;
  readonly signOut: Locator;
  readonly checkYourAnswersHeader: Locator;

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
    await this.continueButton.click();

  }

  async waitForAllUploadsToBeCompleted() {
    let locs = await this.page.getByText('Cancel upload').all();
    for (let i = 0; i < locs.length; i++) {
      await expect(locs[i]).toBeDisabled();
    }
  }

  async waitForTask(taskName: string) {
    expect(await this.reloadAndCheckForText(taskName, 5000, 12)).toBeTruthy();
  }

  async waitForRoleAndAccessTab(userName: string) {
    expect(await this.reloadAndCheckForText(userName, 10000, 3)).toBeTruthy();
  }

  async reloadAndCheckForText(text: string, timeout?: number, maxAttempts?: number): Promise<Boolean> {
    for (let attempt = 0; attempt < (maxAttempts ?? 12); attempt++) {
      await this.page.reload();
      await this.page.waitForLoadState();
      await this.page.waitForTimeout(timeout ?? 5000);
      if (await this.page.getByText(text).isVisible()) {
        return true;
      }
    }

    return false;
  }

  async clickSignOut() {
    await this.signOut.click();
  }
}
