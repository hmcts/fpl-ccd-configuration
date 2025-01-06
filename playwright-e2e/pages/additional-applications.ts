import {expect, type Locator} from "@playwright/test";
import {BasePage} from "./base-page";
import config from "../settings/test-docs/config";

export class AdditionalApplications extends BasePage {
    get otherSpecificOrder(): Locator {

        return this.page.getByText('Other specific order - including C1 and C100 orders, and supplements');
    }

    get c2Order(): Locator {
        return this.page.getByText('C2 - to add or remove someone on a case, or for a specific request to the judge');
    }

    get confidentialC2Order(): Locator {
        return this.page.locator('[for="isC2Confidential_Yes"]');
    }

    get nonConfidentialC2Order(): Locator {
        return this.page.locator('[for="isC2Confidential_No"]');
    }

    get applicant(): Locator {
        return this.page.getByLabel('Select applicant');
    }

    get c1ApplicationType(): Locator {
        return this.page.getByLabel('Select application');
    }

    get applicationForm(): Locator {
        return this.page.getByRole('textbox', {name: 'Upload application'});
    }

    get c2ApplicationForm(): Locator {
        return this.page.getByRole('textbox', {name: 'Upload C2 application'});
    }

    get acknowledgeOtherApplicationForm(): Locator {
        return this.page.locator('[name="temporaryOtherApplicationsBundle_documentAcknowledge"]');
    }

    get acknowledgeC2ApplicationForm(): Locator {
        return this.page.locator('[name="temporaryC2Document_documentAcknowledge"]');
    }

    get sameDay(): Locator {
        return this.page.getByText('On the same day');
    }

    get within2Days(): Locator {
        return this.page.getByText('Within 2 days');
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
        await this.page.waitForTimeout(8000);
        await this.uploadDoc(this.applicationForm, config.testTextFile);
        await this.acknowledgeOtherApplicationForm.check();
        await this.sameDay.click();
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
        await this.page.waitForTimeout(6000);
        await this.uploadDoc(this.c2ApplicationForm);
        await this.expectAllUploadsCompleted();
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

        await this.expectAllUploadsCompleted();
        await this.page.waitForTimeout(6000);
        await this.uploadDoc(this.page.locator('#temporaryC2Document_draftOrdersBundle_0_document'));

        await this.expectAllUploadsCompleted();
        // added hard wait due to EXUI-1194
        // await this.page.waitForTimeout(6000);
        await this.page.locator('#temporaryC2Document_draftOrdersBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();

    }

    public async uploadOtherSupplement() {
        await this.page.locator('#temporaryOtherApplicationsBundle_supplementsBundle').getByRole('button', {name: 'Add new'}).click();
        await this.page.getByLabel('Document name').selectOption('1: C13A_SPECIAL_GUARDIANSHIP');
        await this.page.getByLabel('Notes (Optional)').fill('Notes');
        await this.page.waitForTimeout(6000);
        await this.uploadDoc(this.page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_0_document'));
        await this.page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').click();
        await this.expectAllUploadsCompleted();
    }

    public async uploadOtherSupportingEvidence() {
        await this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle').getByRole('button', {name: 'Add new'}).click();
        await this.page.getByLabel('File name').fill('supporting document');
        await this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_notes').fill('supporting doc notes');
        await this.page.waitForTimeout(6000);
        await this.uploadDoc(this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_document'));
        await this.page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();
        await this.expectAllUploadsCompleted();
    }

    public async payForApplication() {
        await this.page.locator('[for="temporaryPbaPayment_usePbaPayment_Yes"]').check();
        await this.page.getByLabel('Payment by account (PBA) number').fill('PBA1234567');
        await this.page.getByLabel('Customer reference').fill('Customer reference');
        await this.clickContinue();
    }

  public async uploadBasicC2Application(uploadDraftOrder: boolean = true) {
    await this.gotoNextStep('Upload additional applications');
    await this.chooseC2ApplicationType();
    await this.fillC2ApplicationDetails(uploadDraftOrder);
    await this.payForApplication();
    await this.checkYourAnsAndSubmit();
  }
}
