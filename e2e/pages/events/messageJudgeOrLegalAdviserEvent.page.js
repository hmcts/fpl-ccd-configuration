const {I} = inject();

module.exports = {
  fields: {
    isMessageRegardingAdditionalApplications: {
      yes: '#isMessageRegardingAdditionalApplications_Yes',
      no: '#isMessageRegardingAdditionalApplications_No',
    },
    eventOptions: {
      newMessage: '#messageJudgeOption_NEW_MESSAGE',
      reply: '#messageJudgeOption_REPLY',
    },
    additionalApplicationsList: '#additionalApplicationsDynamicList',
    existingMessagesList: '#judicialMessageDynamicList',
    senderEmail: '#judicialMessageMetaData_sender',
    recipientEmail: '#judicialMessageMetaData_recipient',
    replyingToMessage: {
      id: '#judicialMessageReply_isReplying',
      options: {
        yes: '#judicialMessageReply_isReplying_Yes',
        no: '#judicialMessageReply_isReplying_No',
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

  selectMessageRelatedToAdditionalApplication() {
    I.click(this.fields.isMessageRegardingAdditionalApplications.yes);
  },

  selectMessageNotRelatedToAdditionalApplication() {
    I.click(this.fields.isMessageRegardingAdditionalApplications.no);
  },

  async selectAdditionalApplication() {
    const dropdownLabel = await I.grabTextFrom(`${this.fields.additionalApplicationsList} option:nth-child(2)`);
    I.waitForElement(this.fields.additionalApplicationsList);
    await I.runAccessibilityTest();
    I.selectOption(this.fields.additionalApplicationsList, dropdownLabel);
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
