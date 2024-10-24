const {I} = inject();

module.exports = {
  fields: {
    isMessageRegardingDocuments: {
      yes: '#isMessageRegardingDocuments_Yes',
      no: '#isMessageRegardingDocuments_No',
    },
    eventOptions: {
      newMessage: '#messageJudgeOption-NEW_MESSAGE',
      reply: '#messageJudgeOption-REPLY',
    },
    additionalApplicationsList: '#additionalApplicationsDynamicList',
    existingMessagesList: '#judicialMessageDynamicList',
    senderEmail: '#judicialMessageMetaData_sender',
    recipientEmail: '#judicialMessageMetaData_recipient',
    senderType:{
      id:'#judicialMessageMetaData_senderType',
      options:{
        CTCS:'1: CTSC',
        localCourtadmin:'2: LOCAL_COURT_ADMIN',
        legalAdvisor:'3: OTHER',
        hearingJudge:'4: HEARING_JUDGE',
      },
    },
    recipientType:{
      id: '#judicialMessageMetaData_recipientType',
      options:{
        CTCS:'1: CTSC',
        allocateJudge: '2: JUDICIARY',
        localCourtAdmin:'3: LOCAL_COURT_ADMIN',
        legalAdvisor:'4: OTHER',
        HearingJudge:'5: HEARING_JUDGE',
      },
    },
    replyingToMessage: {
      id: '#judicialMessageReply_isReplying',
      options: {
        yes: '#judicialMessageReply_isReplying_Yes',
        no: '#judicialMessageReply_isReplying_No',
      },
    },
    subject: '#judicialMessageMetaData_requestedBy',
    urgency: '#judicialMessageMetaData_urgency',
    latestMessage: '#judicialMessageNote',
    replyMessage: '#judicialMessageReply_latestMessage',
    replyFrom: '#judicialMessageReply_replyFrom',
    replyTo: '#judicialMessageReply_replyTo',
  },

  selectMessageRelatedToAdditionalApplication() {
    I.click(this.fields.isMessageRegardingDocuments.yes);
  },

  selectMessageNotRelatedToAdditionalApplication() {
    I.click(this.fields.isMessageRegardingDocuments.no);
  },

  async selectAdditionalApplication() {
    const dropdownLabel = await I.grabTextFrom(`${this.fields.additionalApplicationsList} option:nth-child(2)`);
    I.waitForElement(this.fields.additionalApplicationsList);
    await I.runAccessibilityTest();
    I.selectOption(this.fields.additionalApplicationsList, dropdownLabel);
  },

  async selectSenderType(user) {
    I.waitForElement(this.fields.senderType.id);
    await I.runAccessibilityTest();
    I.selectOption(this.fields.senderType.id, user);
  },
  async selectRecipientType(user) {
    I.waitForElement(this.fields.recipientType.id);
    await I.runAccessibilityTest();
    I.selectOption(this.fields.recipientType.id,user );

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
