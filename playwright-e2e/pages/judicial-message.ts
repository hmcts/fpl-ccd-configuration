import { type Page, type Locator } from "@playwright/test";
import { BasePage } from "./base-page";


export class JudicialMessage extends BasePage {
    readonly whichApplication: Locator;
    readonly recipient: Locator;
    readonly subject: Locator;
    readonly urgency: Locator;
    readonly recipientEmail: Locator;
    readonly message: Locator;
    readonly messageToReply: Locator;
    readonly haveToReply: Locator;
    readonly reply: Locator;
    readonly documentType: Locator;
    readonly whichDocument: Locator;
    readonly radio: Locator;

    constructor(page: Page) {
        super(page);
        this.whichApplication = page.getByLabel('Which application?');
        this.recipient = page.getByLabel('Recipient', { exact: true }).locator('visible=true');
        this.subject = page.getByLabel('Message subject');
        this.urgency = page.getByText('Is this urgent? (is there any');
        this.recipientEmail = page.getByLabel('Recipient\'s email address');
        this.message = page.getByRole('textbox', { name: 'Message', exact: true });
        this.messageToReply = page.getByLabel('Your messages');
        this.haveToReply = page.getByRole('group', { name: 'Do you need to reply?' });
        this.reply = page.getByRole('textbox', { name: 'Reply' });
        this.documentType = page.getByLabel('Document type');
        this.whichDocument = page.getByLabel('Which document?');
        this.radio = page.getByRole('radio', { name: 'Yes' });
    }

    async sendMessageToAllocatedJudgeWithApplication() {
        await this.page.getByRole('group', { name: 'Is it about an Application or Document?' }).getByLabel('Application').check();
        await this.clickContinue();
        await this.whichApplication.selectOption('C2, 25 March 2021, 3:16pm');
        await this.clickContinue();
        await this.recipient.selectOption('Other Judge/Legal Adviser');
        await this.subject.fill('Message To the legal adviser');
        await this.urgency.click();
        await this.message.fill('Testing');
        await this.clickContinue();
    }

    async sendMessageToAllocatedJudgeWithDocument() {
        await this.page.getByRole('group', { name: 'Is it about an Application or Document?' }).getByLabel('Document').check();
        await this.documentType.selectOption('Skeleton arguments');
        await this.clickContinue();
        await this.whichDocument.selectOption('Test.txt');
        await this.clickContinue();
        await this.recipient.selectOption('Other Judge/Legal Adviser');
        await this.subject.fill('Message To the legal adviser');
        await this.urgency.click();
        await this.message.fill('message send to legal adviser');
        await this.clickContinue();
    }

    async judgeReplyMessage() {
        await this.messageToReply.selectOption('Subject 1, 16 November 2023 at 4:51pm, High');
        await this.clickContinue();
        await this.radio.click();
        await this.haveToReply.getByLabel('Yes').click();
        await this.recipient.selectOption('Local Court Admin - Family Court sitting at Swansea');
        await this.reply.fill('Reply CTSC admin about the hearing.');
        await this.clickContinue();
    }

    async CTSCUserCloseMessage() {
        await this.messageToReply.selectOption('Subject 1, 1 December 2023 at 2:41pm, High');
        await this.clickContinue();
        await this.haveToReply.getByLabel('No').check();
        await this.clickContinue();
    }
}
