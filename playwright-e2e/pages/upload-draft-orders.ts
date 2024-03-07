import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";


export class UploadDraftOrders extends BasePage {
    uploadPDODraftOrders() {
        throw new Error('Method not implemented.');
    }

    constructor(page: Page) {
        super(page);
    }

    //upload CMO
    async uploadCMODraftOrders() {
        await this.page.getByLabel('Case Management (CMO)').check();
        await this.clickContinue();

        await this.page.getByLabel('Agreed CMO discussed at hearing - judge to check and seal').check();
        await this.page.locator('#pastHearingsForCMO').selectOption('Case management hearing, 3 November 2012');
        await this.clickContinue();

        await this.page.getByRole('textbox', { name: 'Attach CMO' }).setInputFiles('./playwright-e2e/files/draftOrder.docx');
        await this.waitForAllUploadsToBeCompleted();
        await this.page.getByLabel('Yes').check();
        await this.clickContinue();

        await expect(this.page.locator('#caseEditForm')).toContainText('draftOrder.docx');
        await this.clickContinue();
        await this.clickSubmit();
    }
    // upload PDO
    async uploadPDOraftOrders() {
        await this.page.getByLabel('Additional order (PDO ETC)').check();
        await this.clickContinue();

        await this.page.locator('#WhichHearingDoesTheOrderRelateTo?').selectOption('Case management hearing, 3 November 2012');
        await this.clickContinue();

        await this.page.getByLabel('Order title').fill('Test');
        await this.page.getByRole('textbox', { name: 'Upload the title' }).setInputFiles('./playwright-e2e/files/Bundle Documents pdf.docx');
        await this.waitForAllUploadsToBeCompleted();
        await this.page.getByLabel('Yes').check();
        await this.clickContinue();

        
        await expect(this.page.locator('#caseEditForm')).toContainText('Documents pdf.docx');
        await this.clickContinue();
        await this.clickSubmit();
    }
}