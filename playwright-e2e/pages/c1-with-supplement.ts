import {type Locator, type Page} from "@playwright/test";
import {BasePage} from "./base-page";
import config from "../settings/test-docs/config";

export class C1WithSupplement extends BasePage {
    constructor(page: Page) {
        super(page);

    }

    get c1WithSupplementHeading(): Locator {
        return this.page.getByRole('heading', {name: 'C1 with supplement'});
    }

    get yesRadio(): Locator {
        return this.page.getByRole('checkbox', {name: 'Yes'});
    }

    get uploadApplicationTextbox(): Locator {
        return this.page.getByRole('textbox', {name: 'Upload application'});
    }

    get onTheSameDay(): Locator {
        return this.page.getByLabel('On the same day');
    }

    get addNewSupplementBundle(): Locator {
        return this.page.locator('#submittedC1WithSupplement_supplementsBundle').getByRole('button', {name: 'Add new'});
    }

    get documentName(): Locator {
        return this.page.getByLabel('Document name');
    }

    get notes(): Locator {
        return this.page.getByLabel('Notes (Optional)');
    }

    get uploadDocument(): Locator {
        return this.page.getByRole('textbox', {name: 'Upload document'});
    }

    get ackRelatedToCase(): Locator {
        return this.page.locator('#submittedC1WithSupplement_supplementsBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE');
    }

    async c1WithSupplementSmokeTest() {
        await this.gotoNextStep('C1 with supplement');
        await this.yesRadio.check();
        await this.uploadApplicationTextbox.setInputFiles(config.testWordFile);
        await this.expectAllUploadsCompleted();
        await this.page.waitForTimeout(6000);
        await this.onTheSameDay.check();
        await this.addNewSupplementBundle.click();
        await this.documentName.selectOption('2: C14_AUTHORITY_TO_REFUSE_CONTACT_WITH_CHILD');
        await this.notes.click();
        await this.notes.fill('notes');
        await this.uploadDocument.setInputFiles(config.testPdfFile);
        await this.page.waitForTimeout(6000);
        await this.ackRelatedToCase.check();
        await this.clickContinue();
        await this.checkYourAnsAndSubmit();

    }
}


