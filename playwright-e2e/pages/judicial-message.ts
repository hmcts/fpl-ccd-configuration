import { type Page, type Locator } from "@playwright/test";
import { BasePage } from "./base-page";


export class JudicialMessage extends BasePage
{
    readonly whichApplication: Locator;
    readonly sender:Locator;
    readonly recipient:Locator;
    readonly subject:Locator;
    readonly urgency:Locator;
    readonly recipientEmail:Locator;
    readonly message:Locator;
    readonly messageToReply:Locator;
    readonly haveToReply : Locator;
    readonly reply: Locator;

    constructor(page:Page){
        super(page);
        this.whichApplication =page.getByLabel('Which application?');
        this.sender =page.getByLabel('Sender', { exact: true });
        this.recipient = page.getByLabel('Recipient', { exact: true });
        this.subject = page.getByLabel('Message subject');
        this.urgency = page.getByLabel('Urgency (Optional)');
        this.recipientEmail =page.getByLabel('Recipient\'s email address');
        this.message = page.getByLabel('Message');
        this.messageToReply = page.getByLabel('Your messages');
        this.haveToReply = page.getByRole('group', { name: 'Do you need to reply?' });
        this.reply = page.getByRole('textbox', { name: 'Reply' });

    }

    async sendMessageToAllocatedJudge(){
        await this.page.getByRole('group',{name: 'Is it about an Application?'}).getByLabel('Yes').check();
        await this.whichApplication.selectOption('C2, 25 March 2021, 3:16pm');
        await this.sender.selectOption('CTSC');
        await this.recipient.selectOption('Allocated Judge');
        await this.recipientEmail.fill('Judge@email.com');
        await this.subject.fill('Message To the allocated Judge');
        await this.urgency.fill('Urgent');
        await this.clickContinue();
        await this.message.fill('message send to allocated Judge');
        await this.clickContinue();

    }

    async judgeReplyMessage(){
        await this.messageToReply.selectOption('Subject 1, 16 November 2023 at 4:51pm, High');
        await this.clickContinue();
        await this.haveToReply.getByLabel('Yes').check();
        await this.reply.fill('Reply CTSC admin about the hearing.');
        await this.clickContinue();
    }

async CTSCUserCloseMessage(){
    await this.messageToReply.selectOption('Subject 1, 1 December 2023 at 2:41pm, High');
    await this.clickContinue();
    await this.haveToReply.getByLabel('No').check();
    await this.clickContinue();
}
}
