import { type Page, type Locator } from "@playwright/test";
import { BasePage } from "./base-page";


export class JudicialMessage extends BasePage
{
    //readonly whichApplication: Locator;
   // readonly sender:Locator;
   // readonly recipient:Locator;
  //  readonly subject:Locator;
  //  readonly urgency:Locator;
  //  readonly recipientEmail:Locator;
  //  readonly message:Locator;
   // readonly messageToReply:Locator;
   // readonly haveToReply : Locator;
   // readonly reply: Locator;
  //  readonly documentType: Locator;
   // readonly whichDocument: Locator;

    constructor(page:Page){
        super(page);
       // this.whichApplication =this.currentPage.;
       // this.sender = this.currentPage.;
      //  this.recipient = this.currentPage.getByLabel('Recipient', { exact: true });
       /// this.subject = this.currentPage.getByLabel('Message subject');
      //  this.urgency = this.currentPage.getByLabel('Urgency (Optional)');
       // this.recipientEmail =this.currentPage.getByLabel('Recipient\'s email address');
       // this.message = this.currentPage.getByLabel('Message', { exact: true });
       // this.messageToReply = this.currentPage.getByLabel('Your messages');
      //  this.haveToReply = this.currentPage.getByRole('group', { name: 'Do you need to reply?' });
      //  this.reply = this.currentPage.getByRole('textbox', { name: 'Reply' });
      //  this.documentType = this.currentPage.getByLabel('Document type');
      //  this.whichDocument = this.currentPage.getByLabel('Which document?');
    }

    async sendMessageToAllocatedJudgeWithApplication(){
        await this.currentPage.getByRole('group',{name: 'Is it about an Application or Document?'}).getByLabel('Application').check();
        await this.clickContinue();
        await this.currentPage.getByLabel('Which application?').selectOption('C2, 25 March 2021, 3:16pm');
        await this.clickContinue();
        await this.currentPage.getByLabel('Sender', { exact: true }).selectOption('CTSC');
        await this.currentPage.getByLabel('Recipient', { exact: true }).selectOption('Allocated Judge');
        await this.currentPage.getByLabel('Recipient\'s email address').click();
        await this.currentPage.getByLabel('Recipient\'s email address').fill('Judge@email.com');
        await this.currentPage.getByLabel('Message subject').fill('Message To the allocated Judge');
        await this.currentPage.getByLabel('Urgency (Optional)').fill('Urgent');
        await this.currentPage.getByLabel('Message', { exact: true }).fill('message send to allocated Judge');
        await this.clickContinue();
    }

    async sendMessageToAllocatedJudgeWithDocument(){
        await this.currentPage.getByRole('group',{name: 'Is it about an Application or Document?'}).getByLabel('Document').check();
        await this.currentPage.getByLabel('Document type').selectOption('Skeleton arguments');
        await this.clickContinue();
        await this.currentPage.getByLabel('Which document?').selectOption('Test.txt');
        await this.clickContinue();
        await this.currentPage.getByLabel('Sender', { exact: true }).selectOption('CTSC');
        await this.currentPage.getByLabel('Recipient', { exact: true }).selectOption('Allocated Judge');
        await this.currentPage.getByLabel('Recipient\'s email address').click();
        await this.currentPage.getByLabel('Recipient\'s email address').fill('Judge@email.com');
        await this.currentPage.getByLabel('Message subject').fill('Message To the allocated Judge');
        await this.currentPage.getByLabel('Urgency (Optional)').fill('Urgent');
        await this.currentPage.getByLabel('Message', { exact: true }).fill('message send to allocated Judge');
        await this.clickContinue();
    }

    async judgeReplyMessage(){
        await this.currentPage.getByLabel('Your messages').selectOption('Subject 1, 16 November 2023 at 4:51pm, High');
        await this.clickContinue();
        await this.currentPage.getByRole('group', { name: 'Do you need to reply?' }).getByLabel('Yes').check();
        await this.currentPage.getByRole('textbox', { name: 'Reply' }).fill('Reply CTSC admin about the hearing.');
        await this.clickContinue();
    }

async CTSCUserCloseMessage(){
    await this.currentPage.getByLabel('Your messages').selectOption('Subject 1, 1 December 2023 at 2:41pm, High');
    await this.clickContinue();
    await this.currentPage.getByRole('group', { name: 'Do you need to reply?' }).getByLabel('No').check();
    await this.clickContinue();
}
}
