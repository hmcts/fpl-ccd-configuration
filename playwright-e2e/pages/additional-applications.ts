import {expect, type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";
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
    private applicationType: Locator;
    private C2FormType: Locator;
    private C2Consent: Locator;
    private C2Confidentiality: Locator;
    private whoMakeApplication: any;
    private c2application: Locator;
    private urgentC2Application: Locator;
    private adjournHearing: Locator;
    private waitUntilNextHearing: Locator;
    private supplementDocument: Locator;
    private applicantName: Locator;


    public constructor(page: Page) {
        super(page);
        this.otherSpecificOrder = page.getByText('Other specific application -');
        this.c2Order = page.getByRole('checkbox', {name: 'C2 Application'});
        this.confidentialC2Order = page.getByLabel('Yes')
        this.nonConfidentialC2Order = page.locator('[for="isC2Confidential_No"]');
        this.applicant = page.getByLabel('Who is making this');
        this.c1ApplicationType = page.getByLabel('Select application');
        this.applicationForm = page.getByRole('button', {name: 'Upload C2 application'});
        this.c2ApplicationForm = page.getByRole('button', {name: 'Upload C2 application'});
        this.acknowledgeOtherApplicationForm = page.locator('[name="temporaryOtherApplicationsBundle_documentAcknowledge"]');
        this.acknowledgeC2ApplicationForm = page.locator('[name="temporaryC2Document_documentAcknowledge"]');
        this.sameDay = page.getByText('On the same day');
        this.within2Days = page.getByText('Within 2 days');
        this.selectApplicant = page.getByLabel('Select applicant');
        this.selectApplication = page.getByLabel('What type of C2 application?');
        this.checkbox = page.getByLabel('Yes');
        this.paymentPbaNumber = page.getByRole('textbox', {name: 'Payment by account (PBA) number'});
        this.typeOfC2Application = page.getByLabel('Yes - only the judge or HMCTS');
        this.paymentPbaNumberTextBox = page.getByRole('textbox', {name: 'Payment by account (PBA)'});
        this.paymentPBANumberDynamicList = page.locator('#temporaryPbaPayment_pbaNumberDynamicList');
        this.applyOnline = page.getByRole('radio', {name: 'Apply online'});
        this.uploadPaperForm = page.getByRole('radio', {name: 'Upload a paper form'});
        this.evidenceConsent = page.getByRole('button', {name: 'Evidence of consent'});
        this.partiesConsent = page.getByRole('radio', {name: 'Yes', exact: true});
        this.confirmDocuments = page.getByRole('checkbox', {name: 'Yes'});
        this.uploadApplication = page.getByRole('button', {name: 'Upload application'});
        this.otherApplicant = page.getByRole('textbox', {name: 'Add applicant\'s name'});

        this.applicationType = page.getByRole('group', {name: 'What application are you making?', exact: true});
        this.C2FormType = page.getByRole('group', {
            name: 'Do you want to make the C2 application online or upload a paper form?',
            exact: true
        });
        this.C2Consent = page.getByRole('group', {name: 'Do all other parties consent to this application?'});
        this.C2Confidentiality = page.getByRole('group', {name: 'Is this a confidential application?'});
        this.whoMakeApplication = page.getByLabel('Who is making this');
        this.applicantName = page.getByRole('textbox', {name: 'Add applicant\'s name'})//page.getByRole('group', { name: 'Who is making this application?' });
        this.c2application = page.getByRole('button', {name: 'Upload C2 application'});
        this.confirmDocuments = page.getByRole('group', {name: 'Tick to confirm this document is related to this case'});
        this.urgentC2Application = page.getByRole('group', {name: 'Is there any reason, such as a safeguarding risk or other urgent issue, that requires your application to be considered by a judge on the same day?'});
        this.adjournHearing = page.getByRole('group', {name: 'Are you requesting an adjournment for a scheduled hearing?'});
        this.waitUntilNextHearing = page.getByRole('group', {name: 'Can your application wait to be considered at the next scheduled hearing? '});
        this.supplementDocument = page.locator('#temporaryC2Document_supplementsBundle');

    }

    public async selectApplicationType(Type: string) {
        await this.applicationType.getByRole('checkbox', {name: Type}).click();
    }

    public async selectC2FormType(type: string) {
        await this.C2FormType.getByRole('radio', {name: type}).click();
    }

    public async giveC2AppConsent(type: string) {
        await this.C2Consent.getByRole('radio', {name: type}).click();
        if (type == 'Yes') {
            await this.evidenceConsent.setInputFiles(config.testPdfFile);
            await this.waitForAllUploadsToBeCompleted();
            await this.page.waitForTimeout(6000);

        }
    }

    public async isC2AppConfidential(confidential: string) {
        await this.C2Confidentiality.getByRole('radio', {name: confidential}).click();
    }

    public async selectWhoMakeApplication(applicant: string) {
        await this.whoMakeApplication.selectOption(applicant);
        if (applicant == 'Someone else') {
            await this.applicantName.fill('Moniks');
        }
    }

    public async uploadC2ApplicationForm() {

        await this.c2application.setInputFiles(config.testPdfFile);
        await this.expectAllUploadsCompleted();
        await this.page.waitForTimeout(6000);
        await this.confirmDocuments.getByLabel('Yes').click()
    }


    public async isC2ApplicationUrgent(YesNo: string, reason: string) {
        await this.urgentC2Application.getByRole('radio', {name: YesNo}).click();
        if (YesNo == 'Yes') {
            expect.soft(await this.page.getByLabel('If your application is urgent please call the court and tribunal support centre (CTSC) on 0330 808 4424').isVisible())
            await this.urgentC2Application.getByLabel('Reason').fill(reason);
        }
    }

    public async IsC2ToAdjournHearing(YesNo: string) {
        await this.adjournHearing.getByRole('radio', {name: YesNo}).click();
    }

    public async canC2AppWaitUntilNextHearing(YesNo: string) {
        await this.waitUntilNextHearing.getByRole('radio', {name: YesNo}).click();
    }

    public async uploadSupplementDocument(supplementDocNumber: string, supplementType: string, notes: string) {

        await this.supplementDocument.locator(`button.write-collection-add-item__top`).click();
        await this.page.selectOption(`select#temporaryC2Document_supplementsBundle_${supplementDocNumber}_name`, supplementType);
        if (notes) {
            await this.page.fill(`#temporaryC2Document_supplementsBundle_${supplementDocNumber}_notes`, notes);

        }


        await this.page.locator(`#temporaryC2Document_supplementsBundle_${supplementDocNumber}_document`).setInputFiles(config.testPdfFile);
        await this.waitForAllUploadsToBeCompleted();
        await this.page.waitForTimeout(6000);
        await this.page.locator(`#temporaryC2Document_supplementsBundle_${supplementDocNumber}_documentAcknowledge-ACK_RELATED_TO_CASE`).check();


    }

    public async uploadC2DraftOrder(draftOrderNumber: string, draftOrderTitle: string) {
        await this.page.locator('#temporaryC2Document_draftOrdersBundle').locator(`button.write-collection-add-item__top`).click();

        await this.page.locator(`#temporaryC2Document_draftOrdersBundle_${draftOrderNumber}_title`).fill(draftOrderTitle);
        await this.page.locator(`#temporaryC2Document_draftOrdersBundle_${draftOrderNumber}_document`).setInputFiles(config.testWordFile);
        await this.waitForAllUploadsToBeCompleted();
        await this.page.waitForTimeout(6000);
        await this.page.locator(`#temporaryC2Document_draftOrdersBundle_${draftOrderNumber}_documentAcknowledge-ACK_RELATED_TO_CASE`).check();

    }


    public async uploadSupportingDocument(supportingDocNumber: any, supportingFileName: any, supportingDocNotes: any) {
        await this.page.locator('#temporaryC2Document_supportingEvidenceBundle').locator(`button.write-collection-add-item__top`).click();

        await this.page.locator(`#temporaryC2Document_supportingEvidenceBundle_${supportingDocNumber}_name`).fill(supportingFileName);
        await this.page.locator(`#temporaryC2Document_supportingEvidenceBundle_${supportingDocNumber}_notes`).fill(supportingDocNotes);
        await this.page.locator(`#temporaryC2Document_supportingEvidenceBundle_${supportingDocNumber}_document`).setInputFiles(config.testPdfFile);
        await this.waitForAllUploadsToBeCompleted();
        await this.page.waitForTimeout(6000);
        await this.page.locator(`#temporaryC2Document_supportingEvidenceBundle_${supportingDocNumber}_documentAcknowledge-ACK_RELATED_TO_CASE`).check();
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
        await this.otherSpecificOrder.click();
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
        await this.page.locator('#temporaryC2Document_draftOrdersBundle').getByRole('button', {name: 'Add new'}).click();
        await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_title').fill('Draft order title');
        await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_document').setInputFiles(config.testWordFile);
        await this.expectAllUploadsCompleted();
        await this.page.waitForTimeout(6000);
        await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();

    }

    public async uploadOtherSupplement() {
        await this.page.locator('#temporaryOtherApplicationsBundle_supplementsBundle').getByRole('button', {name: 'Add new'}).click();
        await this.page.getByLabel('Document name').selectOption('1: C13A_SPECIAL_GUARDIANSHIP');
        await this.page.getByLabel('Notes (Optional)').fill('Notes');
        await this.page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_0_document').setInputFiles(config.testWordFile);
        await this.page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').click();
        await this.expectAllUploadsCompleted();
    }

    public async uploadOtherSupportingEvidence() {
        await this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle').getByRole('button', {name: 'Add new'}).click();
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
    }

    public async uploadBasicC2Application(uploadDraftOrder: boolean = true, PBAnumber: string) {
        await this.chooseC2ApplicationType();
        await this.fillC2ApplicationDetails(uploadDraftOrder);
        await this.payForApplication(PBAnumber);
        await this.checkYourAnsAndSubmit();
    }
}
