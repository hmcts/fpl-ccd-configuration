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
  readonly selectApplicant: Locator;
  readonly selectApplication: Locator;
  readonly checkbox: Locator;
  readonly paymentPbaNumber: Locator;
  readonly typeOfC2Application: Locator;
  readonly paymentPbaNumberTextBox: Locator;
  readonly paymentPBANumberDynamicList: Locator;
  readonly applyOnline: Locator;
  readonly uploadPaperForm: Locator;
  readonly evidenceConsent: Locator;
  readonly partiesConsent: Locator;
  readonly confirmDocuments: Locator;
  readonly uploadApplication: Locator;
  readonly otherApplicant: Locator;


  public constructor(page: Page) {
    super(page);
    this.otherSpecificOrder = page.getByText('Other specific application -');
    this.c2Order = page.getByRole('checkbox', { name: 'C2 Application' });
    this.confidentialC2Order = page.getByLabel('Yes')
    this.nonConfidentialC2Order = page.locator('[for="isC2Confidential_No"]');
    this.applicant = page.getByLabel('Who is making this');
    this.c1ApplicationType = page.getByLabel('Select application');
    this.applicationForm = page.getByRole('button', { name: 'Upload C2 application' });
    this.c2ApplicationForm = page.getByRole('button', { name: 'Upload C2 application' });
    this.acknowledgeOtherApplicationForm = page.locator('[name="temporaryOtherApplicationsBundle_documentAcknowledge"]');
    this.acknowledgeC2ApplicationForm = page.locator('[name="temporaryC2Document_documentAcknowledge"]');
    this.sameDay = page.getByText('On the same day');
    this.within2Days = page.getByText('Within 2 days');
    this.selectApplicant = page.getByLabel('Select applicant');
    this.selectApplication = page.getByLabel('What type of C2 application?');
    this.checkbox = page.getByLabel('Yes');
    this.paymentPbaNumber = page.getByRole('textbox', { name: 'Payment by account (PBA) number' });
    this.typeOfC2Application = page.getByLabel('Yes - only the judge or HMCTS');
    this.paymentPbaNumberTextBox = page.getByRole('textbox', { name: 'Payment by account (PBA)' });
    this.paymentPBANumberDynamicList = page.locator('#temporaryPbaPayment_pbaNumberDynamicList');
    this.applyOnline = page.getByRole('radio', { name: 'Apply online' });
    this.uploadPaperForm = page.getByRole('radio', { name: 'Upload a paper form' });
    this.evidenceConsent = page.getByRole('button', { name: 'Evidence of consent' });
    this.partiesConsent = page.getByRole('radio', { name: 'Yes', exact: true });
    this.confirmDocuments = page.getByRole('checkbox', { name: 'Yes' });
    this.uploadApplication = page.getByRole('button', { name: 'Upload application' });
    this.otherApplicant = page.getByRole('textbox', { name: 'Add applicant\'s name' });

  }

  public async chooseOtherApplicationType() {
    await this.otherSpecificOrder.click();
    await this.applicant.selectOption('Someone else');
    await this.otherApplicant.fill('Moniks');
    await this.clickContinue();

  }

  public async chooseC2ApplicationType() {
    await this.c2Order.click();
    await this.applyOnline.click();
    await this.partiesConsent.click();
    await this.evidenceConsent.setInputFiles(config.testPdfFile);
    await this.expectAllUploadsCompleted();
    await this.page.waitForTimeout(6000);
    await this.typeOfC2Application.click();
    await this.applicant.selectOption('Someone else');
    await this.otherApplicant.fill('James');
    await this.clickContinue();
  }

  public async chooseConfidentialC2ApplicationType() {
    await this.c2Order.click();
    await this.applyOnline.click();
    await this.partiesConsent.click();
    await this.evidenceConsent.setInputFiles(config.testPdfFile);
    await this.expectAllUploadsCompleted();
    await this.page.waitForTimeout(6000);
    await this.typeOfC2Application.click();
    await this.applicant.selectOption('Someone else');
    await this.otherApplicant.fill('Dianah');
    await this.clickContinue();
  }

  public async chooseBothApplicationTypes() {
    await this.c2Order.click();
    await this.applyOnline.click();
    await this.evidenceConsent.getByLabel('Yes').click;
    await this.evidenceConsent.setInputFiles(config.testPdfFile);
    await this.expectAllUploadsCompleted();
    await this.page.waitForTimeout(6000);
    await this.typeOfC2Application.click();
    await this.applicant.selectOption('Someone else');
    await this.otherApplicant.fill('Dianah');
    await this.clickContinue();
  }

  public async enterOnlineC2ApplicationDetails() {
    await this.c2Order.click();
    await this.applyOnline.click();
    await this.evidenceConsent.getByLabel('Yes').click;
    await this.evidenceConsent.setInputFiles(config.testPdfFile);
    await this.expectAllUploadsCompleted();
    await this.page.waitForTimeout(6000);
    await this.typeOfC2Application.click();
    await this.applicant.selectOption('Someone else');
    await this.otherApplicant.fill('Dianah');
    await this.clickContinue();
  }

  public async fillOtherApplicationDetails() {
    await this.c1ApplicationType.selectOption('C1 - Change surname or remove from jurisdiction');

    // upload application form
    await this.uploadApplication.setInputFiles(config.testPdfFile);
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
    // other Applicants details for C1 Application
    await this.applicant.selectOption('Swansea City Council, Applicant');
    await this.applicant.selectOption('Someone else');
    await this.otherApplicant.fill('Dianah');
  }

  public async fillC2ApplicationDetails(uploadDraftOrder: boolean = true) {
    // upload application form
    await this.c2ApplicationForm.setInputFiles(config.testPdfFile);
    await this.expectAllUploadsCompleted();
    await this.page.waitForTimeout(6000);
    await this.acknowledgeC2ApplicationForm.check();
    await this.page.getByLabel('Change surname or remove from jurisdiction.').click();
    await this.within2Days.click();

    // TODO - upload supplements, supporting evidence?

    // add new draft order if required
    if (uploadDraftOrder) {
      await this.uploadDraftOrder();
    }

    await this.clickContinue();
  }

  public async uploadDraftOrder() {
    await this.page.locator('#temporaryC2Document_draftOrdersBundle').getByRole('button', { name: 'Add new' }).click();
    await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_title').fill('Draft order title');
    await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_document').setInputFiles(config.testWordFile);
    await this.expectAllUploadsCompleted();
    await this.page.waitForTimeout(6000);
    await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();

  }

  public async uploadOtherSupplement() {
    await this.page.locator('#temporaryOtherApplicationsBundle_supplementsBundle').getByRole('button', { name: 'Add new' }).click();
    await this.page.getByLabel('Document name').selectOption('1: C13A_SPECIAL_GUARDIANSHIP');
    await this.page.getByLabel('Notes (Optional)').fill('Notes');
    await this.page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_0_document').setInputFiles(config.testWordFile);
    await this.page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').click();
    await this.expectAllUploadsCompleted();
  }

  public async uploadOtherSupportingEvidence() {
    await this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle').getByRole('button', { name: 'Add new' }).click();
    await this.page.getByLabel('File name').fill('supporting document');
    await this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_notes').fill('supporting doc notes');
    await this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_document').setInputFiles(config.testPdfFile);
    await this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();
    await this.expectAllUploadsCompleted();
  }

  public async payForApplication(pbaNumber: string) {
    await this.paymentPBANumberDynamicList.selectOption(pbaNumber);
    await this.page.getByLabel('Customer reference').fill('Test');
    await this.clickContinue();
  }
  public async ctscPayForApplication() {
    await this.paymentPbaNumberTextBox.fill('PBA0096471');
    await this.page.getByLabel('Customer reference').fill('payments');
    await this.clickContinue();
  }

  public async uploadBasicC2Application(uploadDraftOrder: boolean = true, PBAnumber: string) {
    await this.chooseC2ApplicationType();
    await this.fillC2ApplicationDetails(uploadDraftOrder);
    await this.payForApplication(PBAnumber);
    await this.checkYourAnsAndSubmit();
  }
}
