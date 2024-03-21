import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

//export class UploadDocuments {
export class UploadDocuments extends BasePage {
    readonly addNew: Locator;
    readonly nextStep: Locator;
    readonly manageDocuments: Locator;
    readonly uploadNewDocuments: Locator;
    readonly documentType: Locator;
    readonly uploadADocument: Locator;
    uploadDocumentsSmokeTest: any;

    constructor(page: Page) {
        super(page);
        this.nextStep = page.getByRole('textbox', { name: 'Manage documents' });
        this.uploadNewDocuments = page.getByRole('button', { name: 'Uploads new documents' });
        this.addNew = page.getByRole('button', { name: 'Add new' });
        this.uploadADocument = page.getByRole('textbox', { name: 'Upload a document' });
        this.documentType = page.getByRole('textbox', { name: 'Document type' });
        this.manageDocuments = page.getByRole('textbox', { name: 'Manage documents' });
    }
    
    async uploadDocuments() {
        await this.page.getByRole('link', { name: 'Upload documents' }).click();
        await this.page.getByLabel('Type of document').selectOption('3: CARE_PLAN');
        await this.page.getByRole('textbox', { name: 'Upload a file' }).click();
        await this.page.getByRole('textbox', { name: 'Upload a file' }).setInputFiles(config.testWordFile);
        await this.page.getByLabel('Yes - restrict to this group').check();
        await this.page.getByRole('button', { name: 'Add new' }).nth(1).click();
        await this.page.locator('#temporaryApplicationDocuments_1_documentType').selectOption('2: SWET');
        await this.page.getByRole('textbox', { name: 'Let us know what you are' }).click();
        await this.page.getByRole('textbox', { name: 'Let us know what you are' }).fill('test');

        await this.page.locator('#temporaryApplicationDocuments_1_document').click();
        await this.page.locator('#temporaryApplicationDocuments_1_document').setInputFiles(config.testWordFile);
        await this.page.locator('#temporaryApplicationDocuments_1_confidential-CONFIDENTIAL').check();
        await this.page.getByLabel('Give details of documents to').click();
        await this.waitForAllUploadsToBeCompleted();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}

















