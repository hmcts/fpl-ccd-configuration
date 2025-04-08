import { expect, type Locator, type Page } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

export class AddAndRemoveAdminCaseFlag extends BasePage {
    get uploadAssessmentForm(): Locator {
        return this.page.getByRole('textbox', { name: 'Upload assessment form or' });
    }

    get additionalNotes(): Locator {
        return this.page.getByLabel('Additional notes (Optional)');
    }

    get addOrRemoveFlag(): Locator {
        return this.page.getByRole('link', { name: 'Add or remove case flag' });
    }

    get yes(): Locator {
        return this.page.getByLabel('Yes');
    }

    get no(): Locator {
        return this.page.getByLabel('No', { exact: true });
    }



    public async addCaseFlag() {
        await this.yes.check();
        await this.uploadAssessmentForm.setInputFiles(config.testPdfFile);
        await this.waitForAllUploadsToBeCompleted();
        await this.additionalNotes.click();
        await this.additionalNotes.fill('Case Flag Added');
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }

    async runAddCaseFlagTest() {
        await this.gotoNextStep('Add case flag');
        await this.addCaseFlag();
        await this.tabNavigation('Summary');
    }

    async runRemoveCaseFlagTest() {
        await this.removeCaseFlag();
        await this.tabNavigation('Summary');
    }


    public async removeCaseFlag() {
        await this.addOrRemoveFlag.click();
        await this.no.check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();
    }
}
