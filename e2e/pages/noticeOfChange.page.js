const { I } = inject();

module.exports = {
  noticeOfChange: 'Notice of change',
  fields: {
    caseRefSearch: '#caseRef',
    applicantName: '#applicantName',
    respondentFirstName: '#firstName',
    respondentLastName: '#lastName',
    checkboxGroup: {
      affirmation: '#affirmation',
      notifyEveryParty: '#notifyEveryParty',
    },
  },

  navigate(){
    I.click(this.noticeOfChange);
  },

  enterCaseReference(caseReference) {
    I.fillField(this.fields.caseRefSearch, caseReference);
  },

  enterApplicantName(applicantName) {
    I.fillField(this.fields.applicantName, applicantName);
  },

  enterRespondentName(firstName, lastName) {
    I.fillField(this.fields.respondentFirstName, firstName);
    I.fillField(this.fields.respondentLastName, lastName);
  },

  confirmNoticeOfChange() {
    I.checkOption(this.fields.checkboxGroup.affirmation);
    I.checkOption(this.fields.checkboxGroup.notifyEveryParty);
  },
};
