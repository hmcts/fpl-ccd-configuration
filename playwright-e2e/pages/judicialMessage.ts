import { type Page, type Locator, expect } from "@playwright/test";
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
    readonly continue:Locator;
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
        this.continue = page.getByRole('button', { name: 'Continue' });
        this.haveToReply = page.getByRole('group', { name: 'Do you need to reply?' });
       this.reply = page.getByRole('textbox', { name: 'Reply' });

    }

    async sendMessageToAllocatedJudge(){
        await this.page.getByRole('group',{name: 'Is it about an Application?'}).getByLabel('Yes').check();
        await this.whichApplication.selectOption('C2, 25 March 2021, 3:16pm');
        await this.sender.selectOption('CTSC');
        await this.recipient.selectOption('Allocated Judge');
        await this.page.getByLabel('Recipient\'s email address').click();
        await this.recipientEmail.fill('Judge@email.com');
        await this.subject.fill('Message To the allocated Judge');
        await this.urgency.fill('Urgent');
        await this.continue.click();
        await this.message.click();
        await this.message.fill('message send to allocated Judge');
        await this.continue.click();

    }
    async judgeReplyMessage(){

        await this.messageToReply.selectOption('1: c09eb60e-facc-4af6-9761-3e23f6748673');
        await this.continue.click();
        await this.haveToReply.getByLabel('Yes').check();
        await this.reply.fill('Reply CTSC admin about the hearing.');
        await this.continue.click();

    }




}
