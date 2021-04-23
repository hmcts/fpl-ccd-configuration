const { I } = inject();

module.exports = {
  noticeOfChange: 'Notice of change',
  fields: {
    caseRefSearch: '#caseRef',
    applicantName: '#applicantName',
    respondentFirstName: '#respondentFirstName',
    respondentLastName: '#respondentLastName',
    confirmNoC: '#affirmation',
    notifyEveryParty: '#notifyEveryParty',
  },

  navigate(){
    I.click(this.noticeOfChange);
  },

  async enterCaseReference(caseReference) {
    I.fillField(this.fields.caseRefSearch, caseReference);
    await I.runAccessibilityTest();
  },

  async enterApplicantName(applicantName) {
    I.fillField(this.fields.applicantName, applicantName);
    await I.runAccessibilityTest();
  },

  enterRespondentName(firstName, lastName) {
    I.fillField(this.fields.respondentFirstName, firstName);
    I.fillField(this.fields.respondentLastName, lastName);
  },

  confirmNoticeOfChange() {
    I.checkOption(this.fields.confirmNoC);
    I.checkOption(this.fields.notifyEveryParty);
  },
};
