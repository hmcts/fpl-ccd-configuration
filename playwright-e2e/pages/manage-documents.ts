import { Locator, Page, expect } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

export class ManageDocuments extends BasePage {
    readonly manageDocumentsTest: Locator;
    readonly uploadNewDocuments: Locator;
    readonly documentType: Locator;
    readonly IsThisDocumentConfidential: Locator;
    readonly isTranslationNeeded: Locator;
    readonly InputFiles: Locator;
    readonly NonconfidentialRadioButton: Locator;
    readonly ConfidentialRadioButton: Locator;
    readonly IsDocumentRelatedToCase: Locator;
    readonly RemoveDocumentsTest: Locator;
    readonly uploadedDocumentsTest: Locator;
    readonly thereIsAMistakeOnTheDocument: Locator;
    readonly caseFileviewTest: Locator;
    readonly togglePositionStatements: Locator;
    readonly moreDocumentsOptions: Locator;
    readonly changeFolder: Locator;
    readonly threshold: Locator;

    constructor(page: Page) {
        super(page);
        this.manageDocumentsTest = page.getByRole('heading', { name: 'manage documents', exact: true });
        this.uploadNewDocuments = page.getByRole('group', { name: 'upload new documents' });
        this.documentType = page.getByLabel('Document type');
        this.IsThisDocumentConfidential = page.getByRole('group', { name: 'Is this document  confidential?' });
        this.isTranslationNeeded = page.getByRole('group', { name: 'Is translation needed?' });
        this.InputFiles = page.getByRole('textbox', { name: 'Upload a document' });
        this.NonconfidentialRadioButton = page.getByRole('radio', { name: 'No' });
        this.ConfidentialRadioButton = page.getByRole('radio', { name: 'Yes' });
        this.IsDocumentRelatedToCase = page.getByRole('checkbox', { name: 'Yes' });
        this.RemoveDocumentsTest = page.getByLabel('Remove documents');
        this.uploadedDocumentsTest = page.getByRole('group', { name: '1: hearingDocuments.posStmtList###3ad0ca08-1c4c-48' });
        this.thereIsAMistakeOnTheDocument = page.getByLabel('There is a mistake on the');
        this.caseFileviewTest = page.getByText('Case File View');
        this.togglePositionStatements = page.getByRole('button', { name: 'toggle Position Statements' });
        this.moreDocumentsOptions = page.getByRole('button', { name: 'More document options', exact: true });
        this.changeFolder = page.getByText('Change folder');
        this.threshold = page.getByLabel('Threshold', { exact: true });

    }
    async uploadDocuments(type: string) {
        await this.gotoNextStep('Manage documents');

        await this.page.getByLabel('Upload new documents').check();
        await this.clickContinue();

        await this.page.getByRole('textbox', { name: 'Upload a document' })
            .setInputFiles(config.testTextFile);

        await this.page.getByLabel('Document type').selectOption('positionStatement');
        // not confidential
        await this.page.getByRole('radio', { name: 'No' }).check();
        // is on right case
        await this.page.getByRole('checkbox', { name: 'Yes' }).check();

        await this.waitForAllUploadsToBeCompleted();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
    async uploadedDocuments(type: string) {
        await this.manageDocumentsTest.isVisible();
        await expect(this.manageDocumentsTest).toBeVisible();
        await this.uploadNewDocuments.check();
        await this.clickContinue();
        await this.InputFiles.setInputFiles('./playwright-e2e/files/draftOrder.docx');
        await this.documentType.selectOption(type);
        await this.NonconfidentialRadioButton.check();
        await this.IsDocumentRelatedToCase.check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
    async uploadConfidentialDocuments(type: string) {
        await this.manageDocumentsTest.isVisible();
        await expect(this.manageDocumentsTest).toBeVisible();
        await this.uploadNewDocuments.check();
        await this.clickContinue();
        await this.InputFiles.setInputFiles('./playwright-e2e/files/draftOrder.docx');
        await this.documentType.selectOption(type);
        await this.ConfidentialRadioButton.check();
        await this.IsDocumentRelatedToCase.check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
    async RemoveDocuments(type: string) {
        await this.manageDocumentsTest.isVisible();
        await expect(this.manageDocumentsTest).toBeVisible();
        await this.RemoveDocumentsTest.check();
        await this.clickContinue();
        await this.uploadedDocumentsTest.check();
        await this.thereIsAMistakeOnTheDocument.check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
    async caseFileView(type: string) {
        await this.caseFileviewTest.check();
        await this.togglePositionStatements.click();
        await this.moreDocumentsOptions.click();
        await this.changeFolder.click();
        await this.threshold.check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
