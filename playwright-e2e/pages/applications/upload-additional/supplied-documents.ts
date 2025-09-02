import { BasePage } from "../../base-page";
import { Locator, Page } from "@playwright/test";

export class SuppliedDocuments extends BasePage {
    readonly uploadC2ApplicationButton: Locator;
    readonly cancelUploadButton: Locator;

    readonly documentRelatedToCaseGroup: Locator;
    readonly documentRelatedToCaseYesCheckbox: Locator;

    constructor(page: Page) {
        super(page);
        this.uploadC2ApplicationButton = page.getByRole('button', { name: 'Upload C2 application' });
        this.cancelUploadButton = page.getByRole('button', { name: 'Cancel upload' });

        this.documentRelatedToCaseGroup = page.getByRole('group', { name: 'Tick to confirm this document is related to this case'});
        this.documentRelatedToCaseYesCheckbox = this.documentRelatedToCaseGroup.getByRole('checkbox', { name: 'Yes' });
    }

    // document is serialized, must be .docx or .pdf
    async uploadC2Document(document: string): Promise<void> {
        try {
            await this.uploadC2ApplicationButton.setInputFiles(document);
        } catch(exception) {
            throw new Error(`Failed to upload C2 document: "${document}". Ensure the file exists and is in a supported format.\nOriginal error: ${exception}`);
        }
    }

    async checkDocumentRelatedToCaseYes(): Promise<void> {
        await this.documentRelatedToCaseYesCheckbox.check();
    }
}
