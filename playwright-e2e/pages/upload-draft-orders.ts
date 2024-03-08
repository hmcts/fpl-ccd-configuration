import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";


export class UploadDraftOrders extends BasePage {

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
    async uploadAdditionalDraftOrders() {
        await this.page.getByLabel('Additional order (PDO ETC)').check();
        await this.clickContinue();

        await this.page.getByLabel('Which hearing does the order').selectOption('2: f2be08a2-4daf-4aa3-b7ba-95843b4bcb88');
        await this.clickContinue();

        await this.page.getByLabel('Order title').fill('Test');
        await this.page.getByRole('textbox', { name: 'Upload the order' }).setInputFiles('./playwright-e2e/files/draftOrder2.docx');
        await this.page.getByLabel('Yes').nth(0).check();
        await this.waitForAllUploadsToBeCompleted();
        await this.page.getByRole('button', { name: 'Add new' }).nth(1).click();
        await this.page.getByLabel('Order title').nth(1).fill('Test2');
        await this.page.getByRole('textbox', { name: 'Upload the order' }).nth(1).setInputFiles('./playwright-e2e/files/draftOrder.docx');
        await this.waitForAllUploadsToBeCompleted();
        await this.page.getByLabel('Yes').nth(1).click();
        await this.waitForAllUploadsToBeCompleted();
        await this.clickContinue();


        await expect(this.page.locator('#caseEditForm')).toContainText('draftOrder2.docx');
        await expect(this.page.locator('#caseEditForm')).toContainText('draftOrder.docx');
        await this.clickContinue();
        await this.clickSubmit();
    }
}