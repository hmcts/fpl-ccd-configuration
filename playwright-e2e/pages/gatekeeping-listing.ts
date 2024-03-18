import {BasePage} from "./base-page";
import {expect, Page} from "@playwright/test";
import {ManageHearings} from "./manage-hearings";
import {HearingDetailsMixin} from "./mixins/hearing-details-mixin";

export class GatekeepingListing extends HearingDetailsMixin(BasePage)
{
  constructor(page: Page) {
    super(page);
  }

  async  completeJudicialGatekeeping() {
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

  async  completeJudicialGatekeepingWithUploadedOrder() {
    await this.page.getByLabel('Yes').check();
    await this.clickContinue();
    await this.page.getByLabel('Upload a prepared gatekeeping order').check();
    await this.clickContinue();
    await this.page.getByRole('textbox', { name: 'Attach prepared order' })
    .setInputFiles('./playwright-e2e/files/textfile.txt');
    await this.waitForAllUploadsToBeCompleted();
    await this.clickContinue();
    await this.page.getByRole('radio', { name: 'Yes' }).check();
    await this.clickContinue();
    await this.page.getByLabel('The local court admin completes the listing and serves the order').check();
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

  async addHighCourtJudgeAndCompleteGatekeepingLists() {
    await this.page.getByLabel('Search for Judge (Optional)').click();
    await this.page.getByLabel('Search for Judge (Optional)').fill('Arthur Ramirez');
    await this.page.waitForSelector('span:text("His Honour Judge Arthur Ramirez (HHJ.Arthur.Ramirez@ejudiciary.net)")');
    await this.page.getByText('His Honour Judge Arthur Ramirez (HHJ.Arthur.Ramirez@ejudiciary.net)').click();
    await this.clickContinue();
    await this.completeHearingDetails();
    await this.page.getByRole('radio', { name: 'Yes' }).check();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
    await expect(this.page.getByText('has been updated with event: List Gatekeeping Hearing')).toBeVisible();
  }
}
