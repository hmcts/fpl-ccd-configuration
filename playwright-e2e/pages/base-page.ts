import {expect, type Locator, Page} from "@playwright/test";
import {urlConfig} from "../settings/urls";
import config from "../settings/test-docs/config.ts";

export class BasePage {
    get page(): Page {
        return this._page;
    }

    public _page: Page;


  constructor(page: Page) {
this._page = page;
          }

  async switchUser(page:Page)       {
          this._page =page;
     }

  async gotoNextStep(eventName: string) {
      await expect(async () => {
          await this.page.reload();
          await this.page.getByLabel('Next step').selectOption(eventName);
          await this.page.getByRole('button', { name: 'Go', exact: true }).click({clickCount:2,delay:300});
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
    await expect(this.page.getByRole('heading', { name: 'Check your answers' })).toBeVisible();
    await this.page.getByRole('button', { name: 'Save and Continue'}).click();
  }

  async tabNavigation(tabName: string) {
    await this.page.getByRole('tab', { name: tabName }).click();
  }

  async clickContinue() {
    await this.page.getByRole('button', { name: 'Continue' }).click();
  }

  async waitForAllUploadsToBeCompleted() {
    const locs = await this._page.getByText('Cancel upload').all();
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
    await this.page.getByText('Sign out').click();
  }

  async clickSubmit() {
    await this.page.getByRole('button', { name: 'Submit' }).click();
  }

  async enterPostCode(postcode:string){
      await this.page.getByRole('textbox', { name: 'Enter a UK postcode' }).fill(postcode);
      await this.page.getByRole('button', { name: 'Find address' }).click();
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
    async navigateTOCaseDetails(caseNumber: string) {
        await expect(async () => {
            await this.page.goto(`${urlConfig.frontEndBaseURL}/case-details/${caseNumber}`);
            expect(this.page.getByText(this.hypenateCaseNumber(caseNumber))).toBeVisible();
            await this.page.reload();
        }).toPass()
    }
    public async uploadDoc(locator : Locator,file:string = config.testPdfFile ){
        await expect(async  ()=>{
            await this.page.waitForTimeout(6000);
            await locator.setInputFiles(file);
            await this.expectAllUploadsCompleted();
            await  expect( this.page.getByText('rate limited')).toHaveCount(0);
        }).toPass();
    }
    hypenateCaseNumber(caseNumber: string) {
        let hypenatedCaseNumber: string;
        hypenatedCaseNumber = caseNumber.slice(0, 4) + "-" + caseNumber.slice(4, 8) + "-" + caseNumber.slice(8, 12) + "-" + caseNumber.slice(12, 16);
        return hypenatedCaseNumber
    }
}
