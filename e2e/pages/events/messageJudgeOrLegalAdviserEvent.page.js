const {I} = inject();

module.exports = {
  fields: {
    messageRegardingC2: {
      yes: '#isMessageRegardingC2-Yes',
      no: '#isMessageRegardingC2-Yes',
    },
    c2List: '#c2DynamicList',
    recipientEmail: '#judicialMessageMetaData_recipient',
    senderEmail: '#judicialMessageMetaData_sender',
    urgency: '#judicialMessageMetaData_urgency',
    note: '#judicialMessageNote',
  },

  relatedMessageToAC2() {
    I.click(this.fields.messageRegardingC2.yes);
  },

  selectC2(C2) {
    I.selectOption(this.fields.c2List, C2);
  },

  enterRecipientEmail(email) {
    I.fillField(this.fields.recipientEmail, email);
  },

  enterSenderEmail(email) {
    I.fillField(this.fields.senderEmail, email);
  },

  enterUrgency(urgency) {
    I.fillField(this.fields.urgency, urgency);
  },

  enterMessageNote(note) {
    I.fillField(this.fields.note, note);
  },
};
