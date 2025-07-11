
import { expect, Page } from "@playwright/test";
import { HearingDetailsMixin } from "./mixins/hearing-details-mixin";
import config from "../settings/test-docs/config";

export class GatekeepingListing extends HearingDetailsMixin()
{
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
    await this.page.getByRole('button', { name: 'Attach prepared order' }).setInputFiles(config.testWordFile);
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
    await this.selectjudgeType('Fee paid judge');
    await this.page.getByRole('combobox', {name: 'Search for Judge'}).fill('Arthur Ramirez');
    await this.page.getByText('Mr Arthur Ramirez (HHJ.Arthur').click();
    await this.assertFeePaidJudgeTitle();
    await this.page.getByRole('radio', {name: 'Recorder'}).check();
    await this.clickContinue();
    await this.completeHearingDetails();
    await this.page.getByRole('radio', {name: 'Yes'}).check();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
    await expect(this.page.getByText('has been updated with event: List Gatekeeping Hearing')).toBeVisible();
  }

    async completeUrgentDirectionsOrder() {
        await this.page.getByRole('radio', {name: 'Create the urgent directions'}).check();
        await this.clickContinue();
        await this.page.getByRole('checkbox', {name: 'Attend the pre-hearing and'}).check();
        await this.page.getByRole('checkbox', {name: 'Lodge a bundle'}).check();
        await this.page.getByRole('checkbox', {name: 'Reduce time for service of'}).check();
        await this.page.getByRole('checkbox', {name: 'Appoint a children\'s guardian'}).check();
        await this.page.getByRole('checkbox', {name: 'Arrange interpreters'}).check();
        await this.clickContinue();

        await this.clickContinue();

        await this.page.getByRole('button', {name: 'Add new'}).click();
        await this.page.getByRole('textbox', {name: 'Title'}).fill('Accomation Direction');
        await this.page.getByRole('textbox', {name: 'Description (Optional)'}).fill('To accomadate way from the parent home');
        await this.page.getByLabel('Party responsible').selectOption('Local authority');
        await this.page.getByRole('radio', {name: 'Number of working days before'}).check();
        await this.page.getByRole('textbox', {name: 'Number of days'}).fill('6');
        await this.clickContinue();
        await this.page.getByRole('radio', {name: 'District Judge', exact: true}).check();
        await this.page.getByRole('textbox', {name: 'Last name'}).fill('Judge Damien');
        await this.page.getByRole('textbox', {name: 'Email Address'}).fill('email@email.com');

        await this.clickContinue();
        await this.page.getByRole('radio', {name: 'I complete the listing and'}).check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }

    async completeUDOListing() {
        await this.page.getByRole('radio', {name: 'Emergency protection order'}).check();
        await this.page.locator('#hearingVenue').selectOption('8: 99');
        await this.page.getByRole('checkbox', {name: 'In person'}).check();
        await this.page.getByRole('textbox', {name: 'Add details (Optional)'}).fill('Details');
        await this.fillDateInputs(this.page, new Date(new Date().setMonth(new Date().getMonth() + 3)));
        await this.fillTimeInputs(this.page, '10', '00', '00');
        await this.page.getByText('Specific end date and time').click();
        await this.page.getByRole('group', {name: 'End date and time'}).getByLabel('Day').fill(new Date(new Date().setMonth(new Date().getMonth() + 5)).getDate().toString());
        await this.page.getByRole('group', {name: 'End date and time'}).getByLabel('Month').fill(new Date(new Date().setMonth(new Date().getMonth() + 5)).getMonth().toString());
        await this.page.getByRole('group', {name: 'End date and time'}).getByLabel('Year').fill(new Date(new Date().setMonth(new Date().getMonth() + 5)).getFullYear().toString());
        await this.page.getByRole('group', {name: 'End date and time'}).getByLabel('Hour').fill('10');
        await this.clickContinue();

        await this.page.getByRole('radio', {name: 'Salaried judge'}).check();
        await this.page.getByRole('combobox', {name: 'Search for Judge'}).fill('cra');
        await this.page.getByText('District Judge (MC) Craig').click();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
