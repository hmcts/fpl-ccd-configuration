const {I} = inject();

module.exports = {
  fields: {
    messageRegardingC2: {
      yes: '#isMessageRegardingC2-Yes',
      no: '#isMessageRegardingC2-Yes',
    },
    eventOptions: {
      newMessage: '#messageJudgeOption-NEW_MESSAGE',
      reply: '#messageJudgeOption-REPLY',
    },
    c2List: '#c2DynamicList',
    existingMessagesList: '#judicialMessageDynamicList',
    recipientEmail: '#judicialMessageMetaData_recipient',
    about: '#judicialMessageMetaData_about',
    urgency: '#judicialMessageMetaData_urgency',
    latestMessage: '#judicialMessageNote',
    replyMessage: '#judicialMessageReply_latestMessage',
  },

  relatedMessageToAC2() {
    I.click(this.fields.messageRegardingC2.yes);
  },

  async selectC2() {
    const dropdownLabel = await I.grabTextFrom(`${this.fields.c2List} option:nth-child(2)`);
    I.waitForElement(this.fields.c2List);
    I.selectOption(this.fields.c2List, dropdownLabel);
  },

  enterRecipientEmail(email) {
    I.fillField(this.fields.recipientEmail, email);
  },

  enterAbout(about) {
    I.fillField(this.fields.about, about);
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
    I.selectOption(this.fields.existingMessagesList, messageLabel);
  },

  enterMessageReply(reply) {
    I.fillField(this.fields.replyMessage, reply);
  },
};
