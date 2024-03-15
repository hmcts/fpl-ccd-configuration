import { Page } from "@playwright/test";
import { BasePage } from "./base-page";

export class ManageDocuments extends BasePage {

  constructor(page: Page) {
    super(page);
  }

  async uploadDocuments(type: string) {
    await this.gotoNextStep('Manage LAs / Transfer to court');

    await this.page.getByLabel('Upload new documents').check();
    await this.clickContinue();

    await this.page.getByRole('textbox', { name: 'Upload a document' })
      .setInputFiles('./playwright-e2e/files/textfile.txt');

    await this.page.getByLabel('Document type').selectOption(type);
    // not confidential
    await this.page.getByRole('radio', { name: 'No' }).check();
    // is on right case
    await this.page.getByRole('checkbox', { name: 'Yes' }).check();

    await this.waitForAllUploadsToBeCompleted();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }
}
