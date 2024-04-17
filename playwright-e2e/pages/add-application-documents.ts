import { type Page, type Locator, expect } from "@playwright/test";
import config from "../settings/test-docs/config";

export class AddApplicationDocuments {

    readonly page: Page;
    readonly applicationDocumentsHeading: Locator;
    readonly addNewButton: Locator;
    readonly typeOfDocument: Locator;
    readonly chooseFileButton: Locator;
    readonly giveDetailsText: Locator;

    public constructor(page: Page) {
      this.page = page;
      this.applicationDocumentsHeading = page.getByRole('heading', { name: 'Application documents' });
      this.addNewButton = page.getByRole('button', { name: 'Add new' });
      this.typeOfDocument = page.getByLabel('Type of document');
      this.chooseFileButton = page.locator('input#temporaryApplicationDocuments_0_document').first();
      this.giveDetailsText = page.getByLabel('Give details of documents to follow, including why you\'re not sending them now, and when you think they\'ll be ready. (Optional)');
    }

    async uploadDocumentSmokeTest() {
        await this.applicationDocumentsHeading.isVisible();
        await this.addNewButton.first().isVisible();
        await this.typeOfDocument.isVisible();
        await this.typeOfDocument.selectOption('8: BIRTH_CERTIFICATE');
        await this.page.locator('input#temporaryApplicationDocuments_0_document').first().setInputFiles(config.testPdfFile);
        // Wait for the "Uploading..." process to finish otherwise step will fail
        await this.page.locator('span.error-message:text("Uploading...")').isVisible();
        await expect(this.page.locator('span.error-message:text("Uploading...")')).toBeHidden();
        await this.giveDetailsText.isVisible();
        await this.giveDetailsText.fill('Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.');
        await this.page.getByRole('button', { name: 'Continue' }).click();
        await this.page.getByRole('heading', { name: 'Check your answers' }).click();
        await this.page.getByRole('button', { name: 'Save and continue' }).click();
      }
}
