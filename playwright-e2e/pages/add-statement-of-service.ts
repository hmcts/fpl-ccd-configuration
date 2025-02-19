import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class AddStatementOfService extends BasePage {

    readonly addStatementOfService: Locator;
    readonly nameOfRecipient: Locator;
    readonly doYouHaverecipientAddress: Locator;
    readonly documents: Locator;
    readonly dobDay: Locator;
    readonly dobMonth: Locator;
    readonly dobYear: Locator;
    readonly timeSent: Locator;
    readonly howWhereTheySent: Locator;
    readonly recipientEmail: Locator;
    readonly declarationCheckBox: Locator;
    readonly continue: Locator;
    readonly saveAndContinue: Locator;

    public constructor(page: Page) {
        super(page);
        this.addStatementOfService = page.getByRole('heading', { name: 'Add statement of service (c9)', exact: true });
        this.nameOfRecipient = page.getByLabel('Name of recipient');
        this.doYouHaverecipientAddress = page.getByRole('group', { name: 'Do you have the recipient' });
        this.documents = page.getByLabel('Documents');
        this.dobDay = page.getByLabel('Day');
        this.dobMonth = page.getByLabel('Month');
        this.dobYear = page.getByLabel('Year');
        this.timeSent = page.getByLabel('Time sent');
        this.howWhereTheySent = page.getByRole('group', { name: 'How were they sent?' });
        this.recipientEmail = page.getByLabel('Recipient\'s email address');
        this.declarationCheckBox = page.getByLabel('I agree with this statement');
        this.continue = page.getByRole('button', { name: 'Continue' });
        this.saveAndContinue = page.getByRole('button', { name: 'Save and continue' });
    }

    async UploadAddStatementOfService() {
        await expect(this.addStatementOfService).toBeVisible();
        await expect(this.nameOfRecipient).toBeVisible();
        await this.nameOfRecipient.fill('Tom Jones');
        await this.doYouHaverecipientAddress.getByLabel('No').click();
        await this.documents.fill('test');
        await this.dobDay.fill('13');
        await this.dobMonth.fill('02');
        await this.dobYear.fill('2025');
        await this.timeSent.fill('2:30pm');
        await this.howWhereTheySent.getByLabel('email', { exact: true }).click();
        await this.recipientEmail.fill('me@you.com');
        await this.declarationCheckBox.click();
        await this.continue.click();
        await this.saveAndContinue.click();
    }
}
