import { BasePage } from "./base-page";
import { expect, Page } from "@playwright/test";
import { HearingDetailsMixin } from "./mixins/hearing-details-mixin";
import config from "../settings/test-docs/config";

export class GatekeepingListing extends HearingDetailsMixin() {
  constructor(page: Page) {
    super(page);
  }

  async completeJudicialGatekeeping() {
    await this.page.getByLabel('Yes').check();
    await this.clickContinue();
    await this.page.getByLabel('Create the gatekeeping order').check();
    await this.clickContinue();
    await this.page.getByLabel('Request permission for expert').check();
    await this.page.getByLabel('Send documents to all parties').check();
    await this.page.getByLabel('Send response to threshold').check();
    await this.page.getByLabel('Appoint a children\'s guardian').check();
    await this.page.getByLabel('Object to a request for').check();
    await this.page.getByLabel('Arrange interpreters').check();
    await this.clickContinue();
    await this.clickContinue();
    await this.clickContinue();
    await this.page.getByRole('radio', { name: 'Yes' }).check();
    await this.clickContinue();
    await this.page.getByLabel('I complete the listing and').check();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
    await expect(this.page.getByText('has been updated with event: Judicial Gatekeeping')).toBeVisible();
  }

  async completeJudicialGatekeepingWithUploadedOrder() {
    await this.page.getByLabel('Yes').check();
    await this.clickContinue();
    await this.page.getByLabel('Upload a prepared gatekeeping order').check();
    await this.clickContinue();
    await this.page.getByRole('textbox', { name: 'Attach prepared order' }).setInputFiles(config.testWordFile);
    await this.waitForAllUploadsToBeCompleted();
    await this.clickContinue();
    await this.page.getByRole('radio', { name: 'Yes' }).check();
    await this.clickContinue();
    await this.page.getByLabel('The local court admin completes the listing and serves the order').check();
    await this.page.getByLabel('Local court admin will be notified by email that they need to list and serve this order. The order will be saved in the "Draft orders tab" until served. (Optional)').fill('CTSC to send Gatekeepind order');
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
    await expect(this.page.getByText('has been updated with event: Judicial Gatekeeping')).toBeVisible();
  }

  async addAllocatedJudgeAndCompleteGatekeepingListing() {
    await this.page.getByLabel('Search for Judge (Optional)').click();
    await this.page.getByLabel('Search for Judge (Optional)').fill('Craig Taylor');
    await this.page.waitForSelector('span:text("District Judge (MC) Craig")');
    await this.page.getByText('District Judge (MC) Craig').click();
    await this.clickContinue();
    await this.completeHearingDetails();
    await this.page.getByRole('radio', { name: 'Yes' }).check();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
    await expect(this.page.getByText('has been updated with event: List Gatekeeping Hearing')).toBeVisible();
  }

  async addHighCourtJudgeAndCompleteGatekeepingListing() {
    await this.page.getByLabel('Search for Judge (Optional)').fill('Arthur Ramirez');
    await this.page.getByText('Mr Arthur Ramirez (HHJ.Arthur').click();
    await this.clickContinue();
    await this.completeHearingDetails();
    await this.page.getByRole('radio', { name: 'Yes' }).check();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
    await expect(this.page.getByText('has been updated with event: List Gatekeeping Hearing')).toBeVisible();
  }
}
