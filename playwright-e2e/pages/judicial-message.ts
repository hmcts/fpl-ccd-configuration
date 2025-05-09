import { type Page, type Locator } from "@playwright/test";
import { BasePage } from "./base-page";


export class JudicialMessage extends BasePage
{
    get whichApplication(): Locator {
        return this.page.getByLabel('Which application?');
    }

    get sender(): Locator {
        return this.page.getByLabel('Sender', { exact: true });
    }

    get recipient(): Locator {
        return this.page.getByLabel('Recipient', { exact: true });
    }

    get subject(): Locator {
        return this.page.getByLabel('Message subject');
    }

    get urgency(): Locator {
        return this.page.getByLabel('Urgency (Optional)');
    }

    get recipientEmail(): Locator {
        return this.page.getByLabel('Recipient\'s email address');
    }

    get message(): Locator {
        return this.page.getByLabel('Message', { exact: true });
    }

    get messageToReply(): Locator {
        return this.page.getByLabel('Your messages');
    }

    get haveToReply(): Locator {
        return this.page.getByRole('group', { name: 'Do you need to reply?' });
    }

    get reply(): Locator {
        return this.page.getByRole('textbox', { name: 'Reply' });
    }

    get documentType(): Locator {
        return this.page.getByLabel('Document type');
    }

    get whichDocument(): Locator {
        return this.page.getByLabel('Which document?');
    }

    constructor(page:Page){
        super(page);
    }

    async sendMessageToAllocatedJudgeWithApplication(){
        await this.page.getByRole('group',{name: 'Is it about an Application or Document?'}).getByLabel('Application').check();
        await this.clickContinue();
        await this.whichApplication.selectOption('C2, 25 March 2021, 3:16pm');
        await this.clickContinue();
        await this.sender.selectOption('CTSC');
        await this.recipient.selectOption('Allocated Judge');
        await this.page.getByLabel('Recipient\'s email address').click();
        await this.recipientEmail.fill('Judge@email.com');
        await this.subject.fill('Message To the allocated Judge');
        await this.urgency.fill('Urgent');
        await this.message.fill('message send to allocated Judge');
        await this.clickContinue();
    }

    async sendMessageToAllocatedJudgeWithDocument(){
        await this.page.getByRole('group',{name: 'Is it about an Application or Document?'}).getByLabel('Document').check();
        await this.documentType.selectOption('Skeleton arguments');
        await this.clickContinue();
        await this.whichDocument.selectOption('Test.txt');
        await this.clickContinue();
        await this.recipient.selectOption('Allocated Judge - Her Honour Judge Moley (moley@example.com)');
        await this.subject.fill('Message To the allocated Judge');
        await this.urgency.fill('Urgent');
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
