import { expect, type Locator, type Page } from "@playwright/test";
import { BasePage } from "./base-page";

export class AdditionalApplications extends BasePage {

  readonly otherSpecificOrder: Locator;
  readonly c2Order: Locator;
  readonly applicant: Locator;
  readonly c1ApplicationType: Locator;
  readonly applicationForm: Locator;
  readonly c2ApplicationForm: Locator;
  readonly acknowledgeOtherApplicationForm: Locator;
  readonly acknowledgeC2ApplicationForm: Locator;
  readonly sameDay: Locator;

  public constructor(page: Page) {
    super(page);
    this.otherSpecificOrder = page.getByText('Other specific order - including C1 and C100 orders, and supplements');
    this.c2Order = page.getByText('C2 - to add or remove someone on a case, or for a specific request to the judge');
    this.applicant = page.getByLabel('Select applicant');
    this.c1ApplicationType = page.getByLabel('Select application');
    this.applicationForm = page.getByRole('textbox', { name: 'Upload application' });
    this.c2ApplicationForm = page.getByRole('textbox', { name: 'Upload C2 application' });
    this.acknowledgeOtherApplicationForm = page.locator('[name="temporaryOtherApplicationsBundle_documentAcknowledge"]');
    this.acknowledgeC2ApplicationForm = page.locator('[name="temporaryC2Document_documentAcknowledge"]');
    this.sameDay = page.getByText('On the same day');
  }

  public async chooseOtherApplicationType() {
    await this.otherSpecificOrder.click();
    await this.applicant.selectOption('Swansea City Council, Applicant');
    await this.clickContinue();
  }

  public async chooseC2ApplicationType() {
    await this.c2Order.click();
    await this.applicant.selectOption('Swansea City Council, Applicant');
    await this.page.getByText('Application by consent. Parties will be notified of this application.').click();
    await this.clickContinue();
  }


  public async fillOtherApplicationDetails() {
    await this.c1ApplicationType.selectOption('C1 - Change surname or remove from jurisdiction');

    // upload application form
    await this.applicationForm.setInputFiles('./playwright-e2e/files/textfile.txt');
    await expect(this.page.getByText('Cancel upload')).toBeDisabled();

    await this.acknowledgeOtherApplicationForm.check();
    await this.sameDay.click();

    // upload supplements, supporting evidence

    await this.clickContinue();
  }

  public async expectAllUploadsCompleted() {
    let locs = await this.page.getByText('Cancel upload').all();
    for (let i = 0; i < locs.length; i++) {
      await expect(locs[i]).toBeDisabled();
    }
  }

  public async fillC2ApplicationDetails() {
    // upload application form
    await this.c2ApplicationForm.setInputFiles('./playwright-e2e/files/textfile.txt');

    await this.expectAllUploadsCompleted();

    await this.acknowledgeC2ApplicationForm.check();
    await this.page.getByLabel('Change surname or remove from jurisdiction.').click();
    await this.sameDay.click();

    // TODO - upload supplements, supporting evidence?

    // add new draft order
    await this.uploadDraftOrder();

    await this.clickContinue();
  }

  public async uploadDraftOrder() {
    await this.page.locator('#temporaryC2Document_draftOrdersBundle').getByRole('button', { name: 'Add new' }).click();
    await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_title').fill('Draft order title');
    await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_document').setInputFiles('./playwright-e2e/files/textfile.txt');
    await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();
    await this.expectAllUploadsCompleted();
  }

  public async payForApplication() {
    await this.page.getByLabel('Yes').check();
    await this.page.getByLabel('Payment by account (PBA) number').fill('PBA1234567');
    await this.page.getByLabel('Customer reference').fill('Customer reference');
    await this.clickContinue();
  }
}
