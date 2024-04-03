import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class C1WithSupplement extends BasePage {
    readonly c1WithSupplementHeading: Locator;
    readonly nextStep: Locator;
    readonly goButton: Locator;
    readonly yesRadio: Locator;
    readonly uploadApplicationTextbox: Locator;
    readonly uploadApplicationInputFile: Locator;
    readonly sameDayLabel: Locator;
    readonly addNewButton: Locator;
    readonly selectDocumentName: Locator;

    readonly notesLabel: Locator;
    readonly uploadDocumentTextbox: Locator;
    readonly uploadDocumentInputFile: Locator;
    readonly ackRelatedToCaseSupplementsBundle: Locator;
    readonly continueButton: Locator;
    readonly continueAndSaveButton: Locator;
    readonly ackRelatedToCaseSupplementsBundleDoc: Locator;

    constructor(page: Page) {
        super(page);
        this.c1WithSupplementHeading = page.getByRole('heading', { name: 'C1 with supplement' });
        this.nextStep = page.getByLabel('Next step');
        this.goButton = page.getByRole('button', { name: 'Go' });
        this.yesRadio = page.getByRole('checkbox', { name: 'Yes' });
        this.uploadApplicationTextbox = page.getByRole('textbox', { name: 'Upload application' });
        this.englishLangRadio = page.getByLabel('English');
        this.needToBeTranslatedQuestion = page.getByText('Does this application need to be translated into Welsh?');
        this.needToBeInWelshYesRadio = page.getByRole('group', { name: 'Does this application need to be translated into Welsh?' }).getByLabel('Yes');
    }
    async c1WithSupplementSmokeTest(caseId: string) {
        await this.nextStep.selectOption('11: Object');
        await this.goButton.click();
        await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/' + caseId + '/trigger/enterC1WithSupplement/enterC1WithSupplement1');
        await this.yesRadio.check();
        await page.getByRole('textbox', { name: 'Upload application' }).click();
        await page.getByRole('textbox', { name: 'Upload application' }).setInputFiles('Bugs[18].docx');
        await page.getByLabel('On the same day').check();
        await page.locator('#submittedC1WithSupplement_supplementsBundle').getByRole('button', { name: 'Add new' }).click();
        await page.getByLabel('Document name').selectOption('2: C14_AUTHORITY_TO_REFUSE_CONTACT_WITH_CHILD');
        await page.getByLabel('Notes (Optional)').click();
        await page.getByLabel('Notes (Optional)').fill('notes');
        await page.getByRole('textbox', { name: 'Upload document' }).click();
        await page.getByRole('textbox', { name: 'Upload document' }).setInputFiles('Bugs.docx');
        await page.locator('#submittedC1WithSupplement_supplementsBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();
        await page.getByRole('button', { name: 'Continue' }).click();
        await page.getByRole('button', { name: 'Save and continue' }).click();
        await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1712155045902805');
        await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1712155045902805#Start%20application');
    }
}


