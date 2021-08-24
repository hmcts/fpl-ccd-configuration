const { I } = inject();

module.exports = {
  fields: {
    addFlag: {
      yes: '#caseFlagAdded_Yes',
      no: '#caseFlagAdded_No'
    },
    redDotAssessmentForm: '#redDotAssessmentForm',
    notes: '#caseFlagNotes'
  },

  addCaseFlag() {
    I.click(this.fields.addFlag.yes);
  },

  removeCaseFlag() {
      I.click(this.fields.addFlag.no);
  },

  async uploadRedDotAssessmentForm(document) {
    I.attachFile(this.fields.redDotAssessmentForm, document);
  },

  addAdditionalNotes(){
    I.fillField(this.fields.notes, 'Additional case flag notes');
  }
};
