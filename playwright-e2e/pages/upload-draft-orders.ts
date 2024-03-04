import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";


export class UploadDraftOrders extends BasePage {

    constructor(page: Page) {
        super(page);
    }

    //upload CMO
    async uploadDraftOrders() {
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
}