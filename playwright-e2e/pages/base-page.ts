import { type Page, type Locator, expect } from "@playwright/test";

export class BasePage {
  readonly nextStep: Locator;
  readonly goButton: Locator;
  readonly page: Page;
  readonly continueButton: Locator;
  readonly previousButton: Locator;
  readonly cancelLink: Locator;
  readonly signOut: Locator;
  readonly checkYourAnswersHeader: Locator;
  readonly saveAndContinue: Locator;
  readonly submit: Locator;
  readonly postCode: Locator;
  readonly findAddress: Locator;
  readonly rateLimit: Locator;
  readonly year: Locator;
  readonly month: Locator;
  readonly day: Locator;
  private dateOfHearing: Locator;
  readonly startButton: Locator;


  constructor(page: Page) {
    this.page = page;
    this.nextStep = page.getByLabel('Next step');
    this.goButton = page.getByRole('button', { name: 'Go', exact: true });
    this.continueButton = page.getByRole("button", { name: 'Continue' });
    this.previousButton = page.getByRole("button", { name: 'Previous' });
    this.cancelLink = page.getByRole("link", { name: 'Cancel' });
    this.signOut = page.getByText('Sign out');
    this.checkYourAnswersHeader = page.getByRole('heading', { name: 'Check your answers' });
    this.saveAndContinue = page.getByRole("button", { name: 'Save and Continue'});
    this.submit = page.getByRole('button', { name: 'Submit' });
    this.postCode = page.getByRole('textbox', { name: 'Enter a UK postcode' });
    this.findAddress = page.getByRole('button', { name: 'Find address' });
    this.rateLimit = page.getByText('Your request was rate limited. Please wait a few seconds before retrying your document upload');
    this.day = page.getByLabel('Day');
    this.month = this.page.getByLabel('Month');
    this.year = this.page.getByLabel(' Year ');
    this.dateOfHearing =  this.page.getByRole('group', { name: 'What is the date of the' });
    this.startButton = page.getByRole('button', { name: 'Start' });
  }

  async gotoNextStep(eventName: string) {
      await expect(async () => {
          await this.page.reload({waitUntil: 'domcontentloaded'});
          await this.nextStep.selectOption(eventName);
          await this.goButton.click();
          await expect(this.page.getByRole('button', { name: 'Previous',exact:true })).toBeVisible();
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
      const tab = this.page.getByRole('tab').filter({ hasText: new RegExp(`^${tabName}$`) });
      await tab.click();
  }

  async clickContinue() {
    await this.continueButton.waitFor({ state: 'attached'});
      await this.continueButton.click({});
  }

  async clickStartButton() {
      await this.startButton.click();
  }

  async clickPreviousButton() {
      await this.previousButton.click();
  }

  async clickCancelLink() {
      await this.cancelLink.click();
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

        await  this.submit.click();

  }
  async clickSaveAndContinue() {
      await this.saveAndContinue.click();
  }
  async enterPostCode(postcode:string): Promise<void> {
      await this.postCode.fill(postcode);

      await Promise.all([
          this.page.waitForResponse(response =>
              response.url().includes('addresses') &&
              response.request().method() === 'GET' &&
              response.status() === 200
          ),
          this.findAddress.click()
      ]);

      const addressDropdown = this.page.locator('select[name="address"]');

      const optionValues = await addressDropdown.locator('option').evaluateAll(options =>
          options.map(option => (option as HTMLOptionElement).value)
      );

      const secondOptionValue = optionValues[1];

      if (!secondOptionValue) {
          throw new Error('No valid second option found in address dropdown');
      }

      await addressDropdown.selectOption(secondOptionValue);
  }

  getCurrentDate():string {
    let date = new Date();
    let year = new Intl.DateTimeFormat('en', {year: 'numeric'}).format(date);
    let month = new Intl.DateTimeFormat('en', {month: 'short'}).format(date);
    let day = new Intl.DateTimeFormat('en', {day: 'numeric'}).format(date);
      return `${day} ${month} ${year}`;
    }

    async fillDateInputs(page: Page, date: Date) {
      await page.getByRole('textbox', {name: 'Day'}).fill(new Intl.DateTimeFormat('en', {day: 'numeric'}).format(date))
      await page.getByRole('textbox', {name: 'Month'}).fill(new Intl.DateTimeFormat('en', {month: 'numeric'}).format(date));
      await page.getByRole('textbox', {name: 'Year'}).fill(new Intl.DateTimeFormat('en', {year: 'numeric'}).format(date));

    }

    async fillTimeInputs(page: Page, hour = '10', min = '00', sec = '00') {
      await page.getByRole('textbox', {name: 'Hour'}).fill(hour);
      await page.locator('#hearingStartDate-minute').fill(min);
      await page.getByRole('textbox', {name: 'Second'}).fill(sec);
    }
    async enterDate(date: Date){
        await this.dateOfHearing.getByText('Day').fill(date.getDay().toString());
        await this.dateOfHearing.getByText('Month').fill(date.getMonth().toString());
        await this.dateOfHearing.getByText('Year').fill(date.getFullYear().toString())


    }

    async getCellValueInTable(tableName: string, cellHeading: string): Promise<string> {
        const table = this.page.getByRole('table', { name: tableName });
        let cellValue = '';

        try {
            const rows = await table.locator('tr').all();
            for (const row of rows) {
                const heading = await row.locator('th').textContent() || '';
                if (heading === cellHeading) {
                    cellValue = await row.locator('td').textContent() || '';
                    break;
                }
            }

            return cellValue;
        } catch (error) {
            console.error(`Error getting table value: ${error}`);
            throw new Error(`Failed to get value for heading "${cellHeading}" in table "${tableName}"`);
        }
    }
}

