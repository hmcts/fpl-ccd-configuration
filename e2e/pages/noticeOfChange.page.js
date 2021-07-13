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

  async navigate(){
    await I.retryUntilExists(() => I.click(this.noticeOfChange), this.fields.caseRefSearch);
  },

  async enterCaseReference(caseReference) {
    I.fillField(this.fields.caseRefSearch, caseReference);
    return await I.runAccessibilityTest();
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

  async userFillsNoC(caseReference, applicantName, firstName, lastName) {
    await this.navigate();
    await this.enterCaseReference(caseReference);
    await I.retryUntilExists(() => I.click('Continue'), this.fields.applicantName);
    await this.enterApplicantName(applicantName);
    this.enterRespondentName(firstName, lastName);
  },

  async userCompletesNoC(caseReference, applicantName, firstName, lastName) {
    await this.userFillsNoC(caseReference, applicantName, firstName, lastName);
    await I.retryUntilExists(() => I.click('Continue'), this.fields.confirmNoC);
    this.confirmNoticeOfChange();
    await I.retryUntilExists(() => I.click('Submit'), '.govuk-panel--confirmation');
    I.see('Notice of change successful');
    await I.retryUntilExists(() => I.click('View this case'), '.case-title');
  },

};
