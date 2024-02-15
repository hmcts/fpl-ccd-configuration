import { type Page, type Locator, expect } from "@playwright/test";
import config from "../settings/test-docs/config";
import path from "path";

export class AddApplicationDocuments {

    readonly page: Page;
    readonly applicationDocumentsHeading: Locator;
    readonly addNewButton: Locator;
    readonly typeOfDocument: Locator;
    readonly chooseFileButton: Locator;
    readonly giveDetailsText: Locator;
    readonly continueButton: Locator;
    readonly checkYourAnswersHeader: Locator;
    readonly saveAndContinueButton: Locator;
  
    public constructor(page: Page) {
      this.page = page;
      this.applicationDocumentsHeading = page.getByRole('heading', { name: 'Application documents' });
      this.addNewButton = page.getByRole('button', { name: 'Add new' });
      this.typeOfDocument = page.getByLabel('Type of document');
      this.chooseFileButton = page.locator('input#temporaryApplicationDocuments_0_document').first();
      this.giveDetailsText = page.getByLabel('Give details of documents to follow, including why you\'re not sending them now, and when you think they\'ll be ready. (Optional)');
      this.continueButton = page.getByRole('button', { name: 'Continue' });
      this.checkYourAnswersHeader = page.getByRole('heading', { name: 'Check your answers' });
      this.saveAndContinueButton = page.getByRole('button', { name: 'Save and continue' });
    }

    async uploadDocumentSmokeTest() {
        await this.applicationDocumentsHeading.isVisible();
        await this.addNewButton.first().isVisible();
        await this.typeOfDocument.isVisible();
        await this.typeOfDocument.selectOption('8: BIRTH_CERTIFICATE');
        await this.page.locator('input#temporaryApplicationDocuments_0_document').first().setInputFiles(config.testPdfFile);

        // const fileChooserPromise = this.page.waitForEvent('filechooser');
        // await this.chooseFileButton.click();
        // const fileChooser = await fileChooserPromise;
        // await fileChooser.setFiles(path.join(__dirname, '../playwright-e2e/settings/test-docs/testPdf.pdf'));

        //await this.chooseFileButton.setInputFiles(config.testPdfFile);
        //await expect(this.page.locator(".error-message")).toHaveCount(0);
        await this.giveDetailsText.isVisible();
        await this.giveDetailsText.fill('Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.');
        await this.continueButton.click();
        await this.checkYourAnswersHeader.isVisible();
        await this.saveAndContinueButton.click();
    }
}