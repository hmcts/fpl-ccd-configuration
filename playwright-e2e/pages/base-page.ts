import { type Page, type Locator, expect } from "@playwright/test";
export class BasePage {
  readonly nextStep: Locator;
  readonly go: Locator;
  readonly page: Page;
  readonly saveAndContinue: Locator;
  readonly continueButton: Locator;
  readonly signOut: Locator;
  readonly checkYourAnswersHeader: Locator;

  constructor(page: Page) {
    this.page = page;
    this.nextStep = page.getByLabel("Next step");
    this.go = page.getByRole("button", { name: "Go" });
    this.saveAndContinue = page.getByRole("button", { name: "Save and continue" });
    this.continueButton = page.getByRole("button", { name: "Continue" });
    this.signOut = page.getByText('Sign out');
    this.checkYourAnswersHeader = page.getByRole('heading', { name: 'Check your answers' });
  }

  async gotoNextStep(eventName: string) {
    await this.nextStep.selectOption(eventName);
    await this.go.click();
  }

  async checkYourAnsAndSubmit() {
    await this.saveAndContinue.click();
  }

  async tabNavigation(tabName: string) {
    await this.page.getByText(tabName).click();
  }

  async clickContinue() {
    await this.continueButton.click();
  }

  async waitForTask(taskName: string) {
    // waits for upto a minute, refreshing every 5 seconds to see if the task has appeared
    // initial reconfiguration could take upto a minute based on the job scheduling
    expect(await this.reloadAndCheckForText(taskName, 5000, 12)).toBeTruthy();
  }

  async waitForRoleAndAccessTab(userName:string) {
    expect(await this.reloadAndCheckForText(userName, 10000, 3)).toBeTruthy();
  }

  async reloadAndCheckForText(text: string, timeout?: number, maxAttempts?: number): Promise<Boolean> {
    // reload the page, wait 5s, see if it's there
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
