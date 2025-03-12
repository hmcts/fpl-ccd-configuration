import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

export class UploadDraftOrders extends BasePage {

    readonly addNewLocator: Locator;
    readonly cmoOptionLocator: Locator;
    readonly otherOptionalLocator: Locator;
    readonly attachCmoLocator: Locator;
    readonly uploadOrder1Locator0: Locator;
    readonly uploadOrder1Locator1: Locator;

    constructor(page: Page) {
        super(page);
        this.addNewLocator = page.getByRole('button', { name: 'Add new' });
        this.uploadOrder1Locator0 = page.locator('#currentHearingOrderDrafts_0_order');
        this.uploadOrder1Locator1 = page.locator('#currentHearingOrderDrafts_1_order');
        this.cmoOptionLocator = page.getByLabel('Case Management (CMO)');
        this.otherOptionalLocator = page.getByLabel('Additional order (PDO ETC)');
        this.attachCmoLocator = page.locator('#uploadedCaseManagementOrder');
    }

    //upload CMO
    async uploadCMODraftOrders() {
        await this.cmoOptionLocator.check();
        await this.clickContinue();

        await this.page.getByLabel('Agreed CMO discussed at hearing - judge to check and seal').check();
        await this.page.locator('#pastHearingsForCMO').selectOption('Case management hearing, 3 November 2012');
        await this.clickContinue();

        await this.attachCmoLocator.setInputFiles(config.testdraftOrder1);
        await this.waitForAllUploadsToBeCompleted();
        await this.page.getByLabel('Yes').check();
        await this.clickContinue();

        await expect(this.page.getByText('draftOrder1.docx')).toBeVisible();
        await this.clickContinue();
        await this.clickSubmit();
    }

    // upload PDO
    async uploadAdditionalDraftOrders() {
        await this.otherOptionalLocator.check();
        await this.clickContinue();

        await this.page.getByLabel('Which hearing does the order').selectOption('2: f2be08a2-4daf-4aa3-b7ba-95843b4bcb88');
        await this.clickContinue();

        await this.page.getByLabel('Order title').fill('Test');
        await this.uploadOrder1Locator0.setInputFiles(config.testdraftOrder1);
        await this.page.getByLabel('Yes').nth(0).check();
        await this.waitForAllUploadsToBeCompleted();
        await this.page.waitForTimeout(6000);
        await this.addNewLocator.nth(1).click();
        await this.page.getByLabel('Order title').nth(1).fill('Test2');
        await this.uploadOrder1Locator1.setInputFiles(config.testdraftOrder2);
        await this.waitForAllUploadsToBeCompleted();
        await this.page.locator('#currentHearingOrderDrafts_1_documentAcknowledge-ACK_RELATED_TO_CASE').click();
        await this.waitForAllUploadsToBeCompleted();
        await this.clickContinue();

        await expect(this.page.getByText('draftOrder2.docx')).toBeVisible();
        await expect(this.page.getByText('draftOrder1.docx')).toBeVisible();

        await this.clickContinue();
        await this.clickSubmit();
    }
}
