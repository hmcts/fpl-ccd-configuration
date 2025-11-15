import { type Page, type Locator, expect } from "@playwright/test";
import config from "../settings/test-docs/config";

export class AddApplicationDocuments {

    readonly page: Page;
    readonly applicationDocumentsHeading: Locator;
    readonly addNewButton: Locator;
    readonly typeOfDocument: Locator;
    readonly chooseFileButton: Locator;
    readonly giveDetailsText: Locator;
    readonly fileUploadButton: Locator;

    public constructor(page: Page) {
      this.page = page;
      this.applicationDocumentsHeading = page.getByRole('heading', { name: 'Application documents' });
      this.addNewButton = page.getByRole('button', { name: 'Add new' });
      this.typeOfDocument = page.getByLabel('Document type');
      this.chooseFileButton = page.locator('input#temporaryApplicationDocuments_0_document').first();
      this.giveDetailsText = page.getByLabel('Give details of any documents you will upload at a later date.');
      this.fileUploadButton = page.getByRole('button', { name: 'Upload a file' });
    }

    async selectTypeOfDocument(option: string): Promise<void> {
        await this.typeOfDocument.selectOption(option);
    }

    async fillGiveDetails(text: string): Promise<void> {
        await this.giveDetailsText.fill(text);
    }

    async uploadDocumentSmokeTest(): Promise<void> {

       await this.fileUploadButton.setInputFiles(config.testPdfFile);
        await this.page.waitForTimeout(2000); // wait for upload to complete

        await this.selectTypeOfDocument('8: BIRTH_CERTIFICATE');
        await this.fillGiveDetails('testing');

        await Promise.all([
            this.page.waitForResponse(response =>
                response.url().includes('/validate?pageId=uploadDocumentsaddApplicationDocuments') &&
                response.request().method() === 'POST' &&
                response.status() === 200
            ),
            await this.page.getByRole('button', { name: 'Continue' }).click()
        ]);

        await Promise.all([
            this.page.waitForResponse(response =>
                response.url().includes('api/wa-supported-jurisdiction/get') &&
                response.request().method() === 'GET' &&
                response.status() === 200
            ),
            await this.page.getByRole('button', { name: 'Save and continue' }).click()
        ]);

    }
}
