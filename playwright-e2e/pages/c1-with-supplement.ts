import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

export class C1WithSupplement extends BasePage {
    readonly c1WithSupplementHeading: Locator;
    readonly yesRadio: Locator;
    readonly uploadApplicationTextbox: Locator;
    readonly onTheSameDay: Locator;
    readonly addNewSupplementBundle: Locator;
    readonly documentName: Locator;
    readonly notes: Locator;

    readonly uploadDocument: Locator;
    readonly ackRelatedToCase: Locator;

    constructor(page: Page) {
        super(page);
        this.c1WithSupplementHeading = page.getByRole('heading', { name: 'C1 with supplement' });
        this.yesRadio = page.getByRole('checkbox', { name: 'Yes' });
        this.uploadApplicationTextbox = page.getByRole('textbox', { name: 'Upload application' });
        this.onTheSameDay = page.getByLabel('On the same day');
        this.addNewSupplementBundle = page.locator('#submittedC1WithSupplement_supplementsBundle').getByRole('button', { name: 'Add new' });
        this.documentName = page.getByLabel('Document name');
        this.notes = page.getByLabel('Notes (Optional)');
        this.uploadDocument = page.getByRole('textbox', { name: 'Upload document' });
        this.ackRelatedToCase = page.locator('#submittedC1WithSupplement_supplementsBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE');
    }
    async c1WithSupplementSmokeTest() {
        await this.gotoNextStep('C1 with supplement');
        await this.yesRadio.check();
        await this.uploadApplicationTextbox.setInputFiles(config.testWordFile);
        await this.expectAllUploadsCompleted();
        await this.page.waitForTimeout(6000);
        await this.onTheSameDay.check();
        await this.addNewSupplementBundle.click();
        await this.documentName.selectOption('2: C14_AUTHORITY_TO_REFUSE_CONTACT_WITH_CHILD');
        await this.notes.click();
        await this.notes.fill('notes');
        await this.uploadDocument.setInputFiles(config.testPdfFile);
        await this.page.waitForTimeout(6000);
        await this.ackRelatedToCase.check();
        await this.continueButton.click();
        await this.saveAndContinue.click();
    }
}


