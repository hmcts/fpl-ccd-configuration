import { Page } from "@playwright/test";
import { BasePage } from "./base-page";

export class AdditionalApplications extends BasePage {

    constructor(page: Page) {
        super(page);
    }

    async uploadBasicC2Application() {
        await this.gotoNextStep('Upload additional applications');
        await this.page.getByLabel('C2 - to add or remove someone').check();
        await this.page.getByLabel('Application by consent.').check();
        await this.page.getByLabel('Select applicant').selectOption('1: applicant');
        await this.clickContinue();
        await this.page.getByRole('textbox', { name: 'Upload C2 application' })
            .setInputFiles('./playwright-e2e/files/textfile.txt');
        await this.page.getByRole('checkbox', { name: 'Yes' }).check();
        await this.page.getByLabel('Within 2 days').check();
        await this.waitForAllUploadsToBeCompleted();
        await this.clickContinue();

        await this.page.getByRole('radio', { name: 'Yes' }).check();
        await this.page.getByLabel('Payment by account (PBA) number').fill('PBA1234567');
        await this.page.getByLabel('Customer reference').fill('test reference');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}