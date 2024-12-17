import { type Page, type Locator, Browser,expect } from "@playwright/test";
import {urlConfig} from "../settings/urls";
import config from "../settings/test-docs/config";

export class BasePage {
 // readonly nextStep: string;
    protected currentPage: Page;


  constructor(page: Page) {
this.currentPage = page;
          }

  async switchUser(page:Page){
this.currentPage =page;
page.pause();

  }

  async gotoNextStep(eventName: string) {
      await expect(async () => {
          await this.currentPage.reload();
          await this.currentPage.getByLabel('Next step').selectOption(eventName);
          await this.currentPage
          await this.currentPage.getByRole('button', { name: 'Go', exact: true }).click({clickCount:2,delay:300});
          await expect(this.currentPage.getByRole('button', { name: 'Previous' })).toBeDisabled();
      }).toPass();
  }

  async expectAllUploadsCompleted() {
    let locs = await this.currentPage.getByText('Cancel upload').all();
    for (let i = 0; i < locs.length; i++) {
        await expect(locs[i]).toBeDisabled();
    }
  }

  async checkYourAnsAndSubmit(){
    await expect(this.currentPage.getByRole('heading', { name: 'Check your answers' })).toBeVisible();
    await this.currentPage.getByRole('button', { name: 'Save and Continue'}).click();
  }

  async tabNavigation(tabName: string) {
    await this.currentPage.getByRole('tab', { name: tabName,exact: true }).click();
  }

  async clickContinue() {
    await this.currentPage.getByRole('button', { name: 'Continue' }).click();
  }

  async waitForAllUploadsToBeCompleted() {
    const locs = await this.currentPage.getByText('Cancel upload').all();
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
      await this.currentPage.reload();
      await this.currentPage.waitForLoadState();
      await this.currentPage.waitForTimeout(timeout ?? 5000);
      if (await this.currentPage.getByText(text).isVisible()) {
        return true;
      }
    }
    return false;
  }

  async clickSignOut() {
    await this.currentPage.getByText('Sign out').click();
  }

  async clickSubmit() {
    await this.currentPage.getByRole('button', { name: 'Submit' }).click();
  }

  async enterPostCode(postcode:string){
      await this.currentPage.getByRole('textbox', { name: 'Enter a UK postcode' }).fill(postcode);
      await this.currentPage.getByRole('button', { name: 'Find address' }).click();
      await this.currentPage.getByLabel('Select an address').selectOption('1: Object');

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
     console.log(await this.currentPage.context().storageState()) ;
        await this.currentPage.goto(`${urlConfig.frontEndBaseURL}/case-details/${caseNumber}`);
    }
}
