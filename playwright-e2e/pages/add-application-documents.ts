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
        await expect(this.applicationDocumentsHeading).toHaveText('Application documents');
        await expect(this.addNewButton.first()).toBeVisible();
        await expect(this.typeOfDocument).toHaveText('Type of document');
        await this.typeOfDocument.selectOption('8: BIRTH_CERTIFICATE');
        await this.page.locator('input#temporaryApplicationDocuments_0_document').first().setInputFiles(config.testPdfFile);
        // Wait for the "Uploading..." process to finish otherwise step will fail
        await expect(this.page.locator('span.error-message:text("Uploading...")')).toBeVisible();
        await expect(this.page.locator('span.error-message:text("Uploading...")')).toBeHidden();
        await expect(this.giveDetailsText).toHaveText('Give details of documents to follow, including why you\'re not sending them now, and when you think they\'ll be ready. (Optional)');
        await this.giveDetailsText.fill('Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.');
        await this.page.getByRole('button', { name: 'Continue' }).click();
        await this.page.getByRole('heading', { name: 'Check your answers' }).click();
        await this.page.getByRole('button', { name: 'Save and continue' }).click();
      }
}
