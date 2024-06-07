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
  readonly dobDayA: Locator;
  readonly dobMonthA: Locator;
  readonly dobYearA: Locator;
  readonly dobDayB: Locator;
  readonly dobMonthB: Locator;
  readonly dobYearB: Locator;

  constructor(page: Page) {
    this.page = page;
    this.nextStep = page.getByLabel("Next step");
    this.goButton = page.getByRole('button', { name: 'Go', exact: true });
    this.continueButton = page.getByRole("button", { name: "Continue" });
    this.signOut = page.getByText('Sign out');
    this.checkYourAnswersHeader = page.getByRole('heading', { name: 'Check your answers' });
    this.saveAndContinue = page.getByRole("button", { name: "Save and Continue"});
    this.submit = page.getByRole('button', { name: 'Submit' });
    
    this.dobDayA = page.getByRole('textbox', { name: 'Day' });
    this.dobMonthA = page.getByRole('textbox', { name: 'Month' });
    this.dobYearA = page.getByRole('textbox', { name: 'Year' });
    // this.dobDay = page.getByLabel('Day');
    // this.dobMonth = page.getByLabel('Month');
    // this.dobYear = page.getByLabel('Year');
    this.dobDayB = page.getByLabel('Day');
    this.dobMonthB = page.getByLabel('Month');
    this.dobYearB = page.getByLabel('Year');
  }

  async gotoNextStep(eventName: string) {
    await this.nextStep.selectOption(eventName);
    await this.goButton.dblclick();
    await this.page.waitForTimeout(20000);
    if (await  this.goButton.isVisible()) {
       await this.goButton.click();
    }
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
    await this.page.getByRole('tab', { name: tabName }).click();
  }

  // async clickContinue() {
  //   await this.continueButton.click();
  // }

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

  async dobFillOutA(date: string, month: string, year: string){
    await this.dobDayA.fill(date);
    await this.dobMonthA.fill(month);
    await this.dobYearA.fill(year);
  }

  async dobFillOutB(date: string, month: string, year: string){
    await this.dobDayB.fill(date);
    await this.dobMonthB.fill(month);
    await this.dobYearB.fill(year);
  }

  // async clickSubmit() {
  //   await this.submit.click();
  // }
}

