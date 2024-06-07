import { expect, type Locator, type Page } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

export class AdditionalApplications extends BasePage {

  readonly otherSpecificOrder: Locator;
  readonly c2Order: Locator;
  readonly confidentialC2Order: Locator;
  readonly nonConfidentialC2Order: Locator;
  readonly applicant: Locator;
  readonly c1ApplicationType: Locator;
  readonly applicationForm: Locator;
  readonly c2ApplicationForm: Locator;
  readonly acknowledgeOtherApplicationForm: Locator;
  readonly acknowledgeC2ApplicationForm: Locator;
  readonly sameDay: Locator;
  readonly within2Days: Locator;

  public constructor(page: Page) {
    super(page);
    this.otherSpecificOrder = page.getByText('Other specific order - including C1 and C100 orders, and supplements');
    this.c2Order = page.getByText('C2 - to add or remove someone on a case, or for a specific request to the judge');
    this.confidentialC2Order = page.locator('[for="isC2Confidential_Yes"]');
    this.nonConfidentialC2Order = page.locator('[for="isC2Confidential_No"]');
    this.applicant = page.getByLabel('Select applicant');
    this.c1ApplicationType = page.getByLabel('Select application');
    this.applicationForm = page.getByRole('textbox', { name: 'Upload application' });
    this.c2ApplicationForm = page.getByRole('textbox', { name: 'Upload C2 application' });
    this.acknowledgeOtherApplicationForm = page.locator('[name="temporaryOtherApplicationsBundle_documentAcknowledge"]');
    this.acknowledgeC2ApplicationForm = page.locator('[name="temporaryC2Document_documentAcknowledge"]');
    this.sameDay = page.getByText('On the same day');
    this.within2Days = page.getByText('Within 2 days');
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
    await this.nonConfidentialC2Order.click();
    await this.clickContinue();
  }

  public async chooseConfidentialC2ApplicationType() {
    await this.c2Order.click();
    await this.applicant.selectOption('Swansea City Council, Applicant');
    await this.page.getByText('Application by consent. Parties will be notified of this application.').click();
    await this.confidentialC2Order.click();
    await this.clickContinue();
  }

  public async chooseBothApplicationTypes() {
    await this.c2Order.click();
    await this.otherSpecificOrder.click();
    await this.applicant.selectOption('Swansea City Council, Applicant');
    await this.page.getByText('Application by consent. Parties will be notified of this application.').click();
    await this.nonConfidentialC2Order.click();
    await this.clickContinue();
  }

  public async fillOtherApplicationDetails() {
    await this.c1ApplicationType.selectOption('C1 - Change surname or remove from jurisdiction');

    // upload application form
    await this.applicationForm.setInputFiles(config.testTextFile);
    await this.expectAllUploadsCompleted();
    await this.page.waitForTimeout(6000);
    await this.acknowledgeOtherApplicationForm.check();
    await this.sameDay.click();

    // upload supplements, supporting evidence
    await this.uploadOtherSupplement();
    await this.page.waitForTimeout(6000);
    await this.uploadOtherSupportingEvidence();
    await this.clickContinue();
  }


  public async expectAllUploadsCompleted() {
    const locs = await this.page.getByText('Cancel upload').all();
    for (let i = 0; i < locs.length; i++) {
      await expect(locs[i]).toBeDisabled();
    }
  }

  public async fillC2ApplicationDetails() {
    // upload application form
    await this.c2ApplicationForm.setInputFiles(config.testTextFile);
    await this.page.waitForTimeout(6000);
    await this.expectAllUploadsCompleted();

    await this.acknowledgeC2ApplicationForm.check();
    await this.page.getByLabel('Change surname or remove from jurisdiction.').click();
    await this.within2Days.click();

    // TODO - upload supplements, supporting evidence?

    // add new draft order
    await this.uploadDraftOrder();

    await this.clickContinue();
  }

  public async uploadDraftOrder() {
    await this.page.locator('#temporaryC2Document_draftOrdersBundle').getByRole('button', { name: 'Add new' }).click();
    await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_title').fill('Draft order title');
    await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_document').setInputFiles(config.testTextFile);
    // added hard wait due to EXUI-1194
    await this.page.waitForTimeout(6000);
    await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();
    await this.expectAllUploadsCompleted();
  }

  public async uploadOtherSupplement() {
    await this.page.locator('#temporaryOtherApplicationsBundle_supplementsBundle').getByRole('button', { name: 'Add new' }).click();
    await this.page.getByLabel('Document name').selectOption('1: C13A_SPECIAL_GUARDIANSHIP');
    await this.page.getByLabel('Notes (Optional)').fill('Notes');
    await this.page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_0_document').setInputFiles(config.testTextFile);
    await this.page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').click();
    await this.expectAllUploadsCompleted();
  }

  public async uploadOtherSupportingEvidence() {
    await this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle').getByRole('button', { name: 'Add new' }).click();
    await this.page.getByLabel('File name').fill('supporting document');
    await this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_notes').fill('supporting doc notes');
    await this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_document').setInputFiles(config.testTextFile);
    await this.expectAllUploadsCompleted();
    await this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();

  }


  public async payForApplication() {
    await this.page.getByLabel('Yes').check();
    await this.page.getByLabel('Payment by account (PBA) number').fill('PBA1234567');
    await this.page.getByLabel('Customer reference').fill('Customer reference');
    await this.clickContinue();
  }
}
