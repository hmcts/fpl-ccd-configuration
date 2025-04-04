import {expect, type Locator} from "@playwright/test";
import config from "../settings/test-docs/config";
import {BasePage} from "./base-page.ts";

export class AddApplicationDocuments extends BasePage {

    get applicationDocumentsHeading(): Locator {
        return this.page.getByRole('heading', {name: 'Application documents'});
    }

    get addNewButton(): Locator {
        return this.page.getByRole('button', {name: 'Add new'});
    }

    get typeOfDocument(): Locator {
        return this.page.getByLabel('Document type');
    }

    get giveDetailsText(): Locator {
        return this.page.getByLabel('Give details of any documents you will upload at a later date.');
    }


    async uploadDocumentSmokeTest() {
        await expect(this.applicationDocumentsHeading).toBeVisible();
        await expect(this.addNewButton.first()).toBeVisible();
        await expect(this.typeOfDocument).toBeVisible();
        await this.typeOfDocument.selectOption('8: BIRTH_CERTIFICATE');
        await this.page.locator('input#temporaryApplicationDocuments_0_document').first().setInputFiles(config.testPdfFile);
        // Wait for the "Uploading..." process to finish otherwise step will fail
        await expect(this.page.locator('span.error-message:text("Uploading...")')).toBeVisible();
        await expect(this.page.locator('span.error-message:text("Uploading...")')).toBeHidden();
        await expect(this.giveDetailsText).toBeVisible();
        await this.giveDetailsText.fill('Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.');
        await this.page.getByRole('button', {name: 'Continue'}).click();
        await this.page.getByRole('heading', {name: 'Check your answers'}).click();
        await this.page.getByRole('button', {name: 'Save and continue'}).click();
    }
}
