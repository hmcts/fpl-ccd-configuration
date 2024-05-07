import { expect, type Locator, type Page } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

export class AddAndRemoveAdminCaseFlag extends BasePage {

    readonly uploadAssessmentForm: Locator;
    readonly additionalNotes: Locator;
    readonly addOrRemoveFlag: Locator;
    readonly yes: Locator;
    readonly no: Locator;

    public constructor(page: Page) {
        super(page);
        this.yes =  page.getByLabel('Yes');
        this.uploadAssessmentForm = page.getByRole('textbox', { name: 'Upload assessment form or' });
        this.additionalNotes =  page.getByLabel('Additional notes (Optional)');
        this.addOrRemoveFlag =  page.getByRole('link', { name: 'Add or remove case flag' });
        this.no =  page.getByLabel('No', { exact: true });
    }

    public async addCaseFlag() {
        await this.yes.check();
        await this.uploadAssessmentForm.setInputFiles(config.testPdfFile);
        await this.waitForAllUploadsToBeCompleted();
        await this.additionalNotes.click();
        await this.additionalNotes.fill('Case Flag Added');
        await this.continueButton.click();
        await this.saveAndContinue.click();
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
        await this.continueButton.click();
        await this.saveAndContinue.click();
    }
}
