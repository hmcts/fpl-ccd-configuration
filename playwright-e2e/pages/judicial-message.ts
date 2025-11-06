import {type Page, type Locator, expect} from "@playwright/test";
import {BasePage} from "./base-page";


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
    readonly closureNote: Locator;

    constructor(page: Page) {
        super(page);
        this.whichApplication = page.getByLabel('Which application?');
        this.recipient = page.getByLabel('Recipient', {exact: true}).locator('visible=true');
        this.subject = page.getByLabel('Message subject');
        this.urgency = page.getByRole('group' ,{name:'Is this urgent?'});
        this.recipientEmail = page.getByLabel('Recipient\'s email address');
        this.message = page.getByLabel('Message', {exact: true});
        this.messageToReply = page.getByLabel('Your messages');
        this.haveToReply = page.getByRole('group', {name: 'Do you need to reply?'});
        this.reply = page.getByRole('textbox', {name: 'Reply'});
        this.documentType = page.getByLabel('Document type');
        this.whichDocument = page.getByLabel('Which document?');
        this.closureNote = page.getByRole('textbox', {name: 'Add closure note (Optional)'});
    }

    async sendMessageToAllocatedJudgeWithApplication() {
        await this.page.getByRole('group', {name: 'Is it about an Application or Document?'}).getByLabel('Application').check();
        await this.clickContinue();
        await this.whichApplication.selectOption('C2, 25 March 2021, 3:16pm');
        await this.clickContinue();
        await this.recipient.selectOption('Other Judge/Legal Adviser');
        await this.subject.fill('To the allocated judge - Regard Hearing');
        await this.urgency.getByText('yes').click();
        await this.message.fill('Allocated judge to decide on the hearing.');
        await this.clickContinue();
    }

    async sendMessageToAllocatedJudgeWithDocument() {
        await this.page.getByRole('group', {name: 'Is it about an Application or Document?'}).getByLabel('Document').check();
        await this.documentType.selectOption('Skeleton arguments');
        await this.clickContinue();
        await this.whichDocument.selectOption('Test.txt');
        await this.clickContinue();
        await this.recipient.selectOption('Other Judge/Legal Adviser');
        await this.subject.fill('To legal adviser - Regard Hearing assistance');
        await this.urgency.getByText('yes').click();
        await this.message.fill('Hearing needs assistance from legal adviser.');
        await this.clickContinue();
    }

    async judgeReplyMessage() {
        await this.messageToReply.selectOption('Hearing urgency -reg, 4 November 2025 at 10:38am, Not urgent');
        await this.clickContinue();
        await this.haveToReply.getByLabel('Yes').check();
        await this.reply.fill('Reply CTSC admin about the hearing.');
        await this.clickContinue();
    }

    async CTSCUserCloseMessage() {
        await this.messageToReply.selectOption('Hearing urgency -reg, 4 November 2025 at 10:38am, Not urgent');
        await this.clickContinue();
        await this.haveToReply.getByLabel('No').check();
        await this.closureNote.fill('Closing as message is actioned');
        await expect.soft(this.page.getByText('This message will now be marked as closed')).toBeVisible();
        await this.clickContinue();
    }

    async assertJudicialMessageHeaders() {
        return Promise.all([
            expect(this.page.getByRole('columnheader', {name: 'Date sentSort Date sent'})).toBeVisible(),
            expect(this.page.getByRole('columnheader', {name: 'FromSort From'})).toBeVisible(),
            expect(this.page.getByRole('columnheader', {name: 'ToSort To'})).toBeVisible(),
            expect(this.page.getByRole('columnheader', {name: 'StatusSort Status'})).toBeVisible(),
            expect(this.page.getByRole('columnheader', {name: 'Message subjectSort Message'})).toBeVisible()
        ]);
    }

    async expandMessageDetails(from: string) {
        await this.page.getByRole('cell', {name: from, exact: true}).locator('span').click();

    }

}
