const {I} = inject();

module.exports = {
  fields: {
    messageRegardingC2: {
      yes: '#isMessageRegardingC2-Yes',
      no: '#isMessageRegardingC2-No',
    },
    eventOptions: {
      newMessage: '#messageJudgeOption-NEW_MESSAGE',
      reply: '#messageJudgeOption-REPLY',
    },
    c2List: '#c2DynamicList',
    existingMessagesList: '#judicialMessageDynamicList',
    senderEmail: '#judicialMessageMetaData_sender',
    recipientEmail: '#judicialMessageMetaData_recipient',
    replyingToMessage: {
      id: '#judicialMessageReply_isReplying',
      options: {
        yes: '#judicialMessageReply_isReplying-Yes',
        no: '#judicialMessageReply_isReplying-No',
      },
    },
    closeMessageLabel: '#judicialMessageReply_closeMessageLabel',
    subject: '#judicialMessageMetaData_requestedBy',
    urgency: '#judicialMessageMetaData_urgency',
    latestMessage: '#judicialMessageNote',
    replyMessage: '#judicialMessageReply_latestMessage',
    replyFrom: '#judicialMessageReply_replyFrom',
    replyTo: '#judicialMessageReply_replyTo',
  },

  selectMessageRelatedToC2() {
    I.click(this.fields.messageRegardingC2.yes);
  },

  selectMessageNotRelatedToC2() {
    I.click(this.fields.messageRegardingC2.no);
  },

  async selectC2() {
    const dropdownLabel = await I.grabTextFrom(`${this.fields.c2List} option:nth-child(2)`);
    I.waitForElement(this.fields.c2List);
    await I.runAccessibilityTest();
    I.selectOption(this.fields.c2List, dropdownLabel);
  },

  enterRecipientEmail(email) {
    I.fillField(this.fields.recipientEmail, email);
  },

  enterSubject(subject) {
    I.fillField(this.fields.subject, subject);
  },

  enterUrgency(urgency) {
    I.fillField(this.fields.urgency, urgency);
  },

  enterMessage(latestMessage) {
    I.fillField(this.fields.latestMessage, latestMessage);
  },

  selectReplyToMessage() {
    I.click(this.fields.eventOptions.reply);
  },

  async selectJudicialMessage() {
    const messageLabel = await I.grabTextFrom(`${this.fields.existingMessagesList} option:nth-child(2)`);
    I.waitForElement(this.fields.existingMessagesList);
    await I.runAccessibilityTest();
    I.selectOption(this.fields.existingMessagesList, messageLabel);
  },

  selectReplyingToJudicialMessage() {
    I.click(this.fields.replyingToMessage.options.yes);
  },

  selectClosingJudicialMessage() {
    I.click(this.fields.replyingToMessage.options.no);
  },

  enterMessageReply(reply) {
    I.fillField(this.fields.replyMessage, reply);
  },
};
