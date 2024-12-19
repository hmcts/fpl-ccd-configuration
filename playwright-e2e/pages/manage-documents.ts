
import { Locator, Page, expect } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

export class ManageDocuments extends BasePage {

    //readonly manageDocumentsTest: Locator;
   // readonly uploadNewDocuments: Locator;
   // readonly documentType: Locator;
 //   readonly isThisDocumentConfidential: Locator;
  //  readonly isTranslationNeeded: Locator;
   // readonly inputFiles: Locator;
  //  readonly nonconfidentialRadioButton: Locator;
   // readonly confidentialRadioButton: Locator;
   // readonly isDocumentRelatedToCase: Locator;
 //   readonly removeDocumentsTest: Locator;
  //  readonly uploadedDocumentsTest: Locator;
  //  readonly thereIsAMistakeOnTheDocument: Locator;
   // readonly caseFileviewTest: Locator;
  //  readonly togglePositionStatements: Locator;
  //  readonly moreDocumentsOptions: Locator;
  //  readonly changeFolder: Locator;
 //   readonly threshold: Locator;
//    readonly addNew: Locator;
 //   readonly no: Locator;

    constructor(page: Page) {
        super(page);
      //  this.manageDocumentsTest = page.getByRole('heading', { name: 'Manage documents', exact: true });
      //  this.uploadNewDocuments = page.getByLabel('Upload new documents');
       // this.documentType = page.getByLabel('Document type');
      //  this.isThisDocumentConfidential = page.getByRole('group', { name: 'Is this document  confidential?' });
      //  this.isTranslationNeeded = page.getByRole('group', { name: 'Is translation needed?' });
      //  this.inputFiles = page.getByRole('textbox', { name: 'Upload a document' });
       // this.nonconfidentialRadioButton = page.getByRole('radio', { name: 'No' });
       // this.confidentialRadioButton = page.getByRole('radio', { name: 'Yes' });
      //  this.isDocumentRelatedToCase = page.getByRole('checkbox', { name: 'Yes' });
       // this.removeDocumentsTest = page.getByLabel('Remove documents');
       // this.uploadedDocumentsTest = page.getByRole('group', { name: '1: hearingDocuments.posStmtList###3ad0ca08-1c4c-48' });
      //  this.thereIsAMistakeOnTheDocument = page.getByLabel('There is a mistake on the');
      //  this.caseFileviewTest = page.getByText('Case File View');
      //  this.togglePositionStatements = page.getByRole('button', { name: 'toggle Position Statements' });
     //   this.moreDocumentsOptions = page.getByRole('button', { name: 'More document options', exact: true });
     //  this.changeFolder = page.getByText('Change folder');
      //  this.threshold = page.getByLabel('Threshold', { exact: true });
    //    this.addNew = page.getByRole('button', { name: 'Add new' });
    //    this.no = page.getByRole('radio', { name: 'No' });

    }
    async uploadDocuments(type: string,isConfidential: string= 'No') {
        await this.currentPage.getByLabel('Upload new documents').check();
        await this.clickContinue();

        await this.currentPage.getByRole('textbox', { name: 'Upload a document' })
            .setInputFiles(config.testTextFile);

        await this.currentPage.getByLabel('Document type').selectOption(type);

        // not confidential
        await this.currentPage.getByRole('radio', { name: `${isConfidential}` }).check();

        // is on right case
        await this.currentPage.getByRole('checkbox', { name: 'Yes'}).check();
        await this.waitForAllUploadsToBeCompleted();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }

    async removeDocuments() {
        await this.currentPage.getByLabel('Remove documents').check();
        await this.clickContinue();
        await this.currentPage.getByLabel('Document type').selectOption('Court correspondence');
        await this.clickContinue();
        await this.currentPage.getByLabel('Uploaded Document').selectOption('mock.pdf');
        await this.currentPage.getByLabel('There is a mistake on the').check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
