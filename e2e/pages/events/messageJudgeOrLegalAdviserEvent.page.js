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

  async selectC2() {
    const dropdownLabel = await I.grabTextFrom(`${this.fields.c2List} option:nth-child(2)`);
    I.waitForElement(this.fields.c2List);
    I.selectOption(this.fields.c2List, dropdownLabel);
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
