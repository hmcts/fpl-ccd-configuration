const { I } = inject();

module.exports = {
  fields: {
    caseExtensionTime: '#caseExtensionTimeList-EightWeekExtension',
    caseExtensionReason: '#caseExtensionReasonList-TimetableForChild',
    caseExtensionComment: '#extensionComments',
    caseExtensionTimeConfirmation: '#caseExtensionTimeConfirmationList-OtherExtension',
    caseExtensionDate: {
      day: '#eightWeeksExtensionDateOther-day',
      month: '#eightWeeksExtensionDateOther-month',
      year: '#eightWeeksExtensionDateOther-year',
    },
  },

  selectCaseExtensionTime() {
    I.click(this.fields.caseExtensionTime);
  },

  selectCaseExtensionReason() {
    I.click(this.fields.caseExtensionReason);
  },

  addComment(comment) {
    I.fillField(this.fields.caseExtensionComment, comment);
  },

  addCaseExtensionTimeConfirmation(){
    I.click(this.fields.caseExtensionTimeConfirmation);
  },

  addCaseExtensionDate(){
    I.fillField(this.fields.caseExtensionDate.day, '10');
    I.fillField(this.fields.caseExtensionDate.month, '10');
    I.fillField(this.fields.caseExtensionDate.year, '2020');
  },
};
