
import { Locator, Page, expect } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";
import {type} from "node:os";

export class ManageDocuments extends BasePage {
    // get manageDocumentsTest(): Locator {
    //     return this.page.getByRole('heading', { name: 'Manage documents', exact: true });
    // }

    // get uploadNewDocuments(): Locator {
    //     return this.page.getByLabel('Upload new documents');
    // }

    // get documentType(): Locator {
    //     return this.page.getByLabel('Document type');
    // }
    //
    // get isThisDocumentConfidential(): Locator {
    //     return this.page.getByRole('group', { name: 'Is this document  confidential?' });
    // }
    //
    // get isTranslationNeeded(): Locator {
    //     return this.page.getByRole('group', { name: 'Is translation needed?' });
    // }
    //
    // get inputFiles(): Locator {
    //     return this.page.getByRole('textbox', { name: 'Upload a document' });
    // }
    //
    // get nonconfidentialRadioButton(): Locator {
    //     return this.page.getByRole('radio', { name: 'No' });
    // }
    //
    // get confidentialRadioButton(): Locator {
    //     return this.page.getByRole('radio', { name: 'Yes' });
    // }
    //
    // get isDocumentRelatedToCase(): Locator {
    //     return this.page.getByRole('checkbox', { name: 'Yes' });
    // }
    //
    // get removeDocumentsTest(): Locator {
    //     return this.page.getByLabel('Remove documents');
    // }
    //
    // get uploadedDocumentsTest(): Locator {
    //     return this.page.getByRole('group', { name: '1: hearingDocuments.posStmtList###3ad0ca08-1c4c-48' });
    // }
    //
    // get thereIsAMistakeOnTheDocument(): Locator {
    //     return this.page.getByLabel('There is a mistake on the');
    // }
    //
    // get caseFileviewTest(): Locator {
    //     return this.page.getByText('Case File View');
    // }
    //
    // get togglePositionStatements(): Locator {
    //     return this.page.getByRole('button', { name: 'toggle Position Statements' });
    // }
    //
    // get moreDocumentsOptions(): Locator {
    //     return this.page.getByRole('button', { name: 'More document options', exact: true });
    // }
    //
    // get changeFolder(): Locator {
    //     return this.page.getByText('Change folder');
    // }

    // get threshold(): Locator {
    //     return this.page.getByLabel('Threshold', { exact: true });
    // }
    //
    // get addNew(): Locator {
    //     return this.page.getByRole('button', { name: 'Add new' });
    // }
    //
    // get no(): Locator {
    //     return this.page.getByRole('radio', { name: 'No' });
    // }

    // private readonly _manageDocumentsTest: Locator;
    // private readonly _uploadNewDocuments: Locator;
    // private readonly _documentType: Locator;
    // private readonly _isThisDocumentConfidential: Locator;
    // private readonly _isTranslationNeeded: Locator;
    // private readonly _inputFiles: Locator;
    // private readonly _nonconfidentialRadioButton: Locator;
    // private readonly _confidentialRadioButton: Locator;
    // private readonly _isDocumentRelatedToCase: Locator;
    // private readonly _removeDocumentsTest: Locator;
    // private readonly _uploadedDocumentsTest: Locator;
    // private readonly _thereIsAMistakeOnTheDocument: Locator;
    // private readonly _caseFileviewTest: Locator;
    // private readonly _togglePositionStatements: Locator;
    // private readonly _moreDocumentsOptions: Locator;
    // private readonly _changeFolder: Locator;
    // private readonly _threshold: Locator;
    // private readonly _addNew: Locator;
    // private readonly _no: Locator;

    // constructor(page: Page) {
    //     super(page);
    //     this._manageDocumentsTest =
    //     this._uploadNewDocuments =
    //     this._documentType =
    //     this._isThisDocumentConfidential =
    //     this._isTranslationNeeded =
    //     this._inputFiles =
    //     this._nonconfidentialRadioButton =
    //     this._confidentialRadioButton =
    //     this._isDocumentRelatedToCase =
    //     this._removeDocumentsTest =
    //     this._uploadedDocumentsTest =
    //     this._thereIsAMistakeOnTheDocument =
    //     this._caseFileviewTest =
    //     this._togglePositionStatements =
    //     this._moreDocumentsOptions =
    //     this._changeFolder =
    //     this._threshold =
    //     this._addNew =
    //     this._no =
    //
    // }
    async uploadDocuments(type: string,isConfidential: string= 'No') {
        await this.page.getByLabel('Upload new documents').check();
        await this.clickContinue();

        await this.page.getByText('Upload a document',{exact:true} )
            .setInputFiles(config.testTextFile);

        await this.page.getByLabel('Document type').selectOption(type);

        // not confidential
        await this.page.getByRole('radio', { name: `${isConfidential}` }).check();

        // is on right case
        await this.page.getByRole('checkbox', { name: 'Yes'}).check();
        await this.waitForAllUploadsToBeCompleted();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }

    async removeDocuments() {
        await this.page.getByLabel('Remove documents').check();
        await this.clickContinue();
        await this.page.getByLabel('Document type').selectOption('Court correspondence');
        await this.clickContinue();
        await this.page.getByLabel('Uploaded Document').selectOption('mock.pdf');
        await this.page.getByLabel('There is a mistake on the').check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
