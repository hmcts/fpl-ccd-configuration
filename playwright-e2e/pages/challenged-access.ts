import {type Page, type Locator} from "@playwright/test";

export class ChallengedAccess {

  readonly requestAccess: Locator;
  readonly page: Page;
  readonly submit: Locator;
  readonly determineConsolidation: Locator;
  readonly viewCaseLink: Locator;

  constructor(page: Page) {
    this.page = page;
    this.requestAccess = page.getByRole('button', {name: 'Request access'});
    this.submit = page.getByRole('button', {name: 'Submit'});
    this.determineConsolidation = page.getByRole('radio', {name: 'To determine if the case needs to be consolidated'});
    this.viewCaseLink = page.getByText("View case file")
  }

  async requestAccessToCase() {
    await this.requestAccess.click();
  }

  async chooseReason() {
    await this.determineConsolidation.click();
    await this.submit.click();
  }

  async viewCase() {
    await this.viewCaseLink.click();
  }

}
