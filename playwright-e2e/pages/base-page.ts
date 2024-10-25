import { type Page, type Locator, expect } from "@playwright/test";

export class BasePage {
  readonly nextStep: Locator;
  readonly goButton: Locator;
  readonly page: Page;
  readonly continueButton: Locator;
  readonly signOut: Locator;
  readonly checkYourAnswersHeader: Locator;
  readonly saveAndContinue: Locator;
  readonly submit: Locator;
  readonly postCode: Locator;
  readonly findAddress: Locator;
  readonly rateLimit: Locator;


  constructor(page: Page) {
    this.page = page;
    this.nextStep = page.getByLabel('Next step');
    this.goButton = page.getByRole('button', { name: 'Go', exact: true });
    this.continueButton = page.getByRole("button", { name: 'Continue' });
    this.signOut = page.getByText('Sign out');
    this.checkYourAnswersHeader = page.getByRole('heading', { name: 'Check your answers' });
    this.saveAndContinue = page.getByRole("button", { name: 'Save and Continue'});
    this.submit = page.getByRole('button', { name: 'Submit' });
    this.postCode = page.getByRole('textbox', { name: 'Enter a UK postcode' });
    this.findAddress = page.getByRole('button', { name: 'Find address' });
    this.rateLimit = page.getByText('Your request was rate limited. Please wait a few seconds before retrying your document upload');
  }

  async gotoNextStep(eventName: string) {
      await expect(async () => {
          await this.page.reload();
          await this.nextStep.selectOption(eventName);
          await this.goButton.click({clickCount:2,delay:300});
          await expect(this.page.getByRole('button', { name: 'Previous' })).toBeDisabled();
      }).toPass();
  }

  async expectAllUploadsCompleted() {
    let locs = await this.page.getByText('Cancel upload').all();
    for (let i = 0; i < locs.length; i++) {
        await expect(locs[i]).toBeDisabled();
    }
  }

  async checkYourAnsAndSubmit(){
    await expect(this.checkYourAnswersHeader).toBeVisible();
    await this.saveAndContinue.click();
  }

  async tabNavigation(tabName: string) {
    await this.page.getByRole('tab', { name: tabName,exact: true }).click();
  }

  async clickContinue() {
    await this.continueButton.click();
  }

  async waitForAllUploadsToBeCompleted() {
    const locs = await this.page.getByText('Cancel upload').all();
    for (let i = 0; i < locs.length; i++) {
      await expect(locs[i]).toBeDisabled();
    }
  }

  async waitForTask(taskName: string) {
    // waits for upto a minute, refreshing every 5 seconds to see if the task has appeared
    // initial reconfiguration could take upto a minute based on the job scheduling
    expect(await this.reloadAndCheckForText(taskName, 5000, 12)).toBeTruthy();
  }

  async waitForRoleAndAccessTab(userName: string) {
    expect(await this.reloadAndCheckForText(userName, 10000, 3)).toBeTruthy();
  }

  async reloadAndCheckForText(text: string, timeout?: number, maxAttempts?: number): Promise<boolean> {
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

  async clickSubmit() {
    await this.submit.click();
  }

  async enterPostCode(postcode:string){
      await this.postCode.fill(postcode);
      await this.findAddress.click();
      await this.page.getByLabel('Select an address').selectOption('1: Object');

  }
    getCurrentDate():string {
        let date = new Date();
        let year = new Intl.DateTimeFormat('en', { year: 'numeric' }).format(date);
        let month = new Intl.DateTimeFormat('en', { month: 'short' }).format(date);
        let day = new Intl.DateTimeFormat('en', { day: 'numeric'}).format(date);
        let todayDate = `${day} ${month} ${year}`;
        return todayDate;
    }
}
