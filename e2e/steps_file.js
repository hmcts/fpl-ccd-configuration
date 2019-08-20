/* global process */
const config = require('./config');

const logIn = require('./pages/loginPage');
const createCasePage = require('./pages/createCase');
const eventSummaryPage = require('./pages/eventSummary');

let baseUrl = process.env.URL || 'http://localhost:3451';

'use strict';

module.exports = function () {
  return actor({
    logInAndCreateCase(username, password) {
      logIn.signIn(username, password);
      this.click('Create Case');
      this.waitForElement(`#cc-jurisdiction > option[value="${config.definition.jurisdiction}"]`);
      createCasePage.populateForm();
      this.continueAndSave();
    },

    continueAndSave() {
      this.click('Continue');
      this.waitForElement('.check-your-answers');
      eventSummaryPage.submit('Save and continue');
    },

    continueAndProvideSummary(summary, description) {
      this.click('Continue');
      this.waitForElement('.check-your-answers');
      eventSummaryPage.provideSummaryAndSubmit('Save and continue', summary, description);
    },

    continueAndSubmit() {
      this.click('Continue');
      this.waitForElement('.check-your-answers');
      eventSummaryPage.submit('Submit');
    },

    seeEventSubmissionConfirmation(event) {
      this.see(`updated with event: ${event}`);
    },

    clickHyperlink(link, urlNavigatedTo) {
      this.click(link);
      this.seeCurrentUrlEquals(urlNavigatedTo);
    },

    seeDocument(title, name, status, reason = '') {
      this.see(title);
      this.see(status);
      if (reason !== '') {
        this.see(reason);
      } else {
        this.see(name);
      }
    },

    seeAnswerInTab(questionNo, complexTypeHeading, question, answer) {
      const complexType = locate(`.//span[text() = '${complexTypeHeading}']`);
      const questionRow = locate(`${complexType}/../../../table/tbody/tr[${questionNo}]`);
      this.seeElement(locate(`${questionRow}/th/span`).withText(question));
      if (Array.isArray(answer)) {
        let ansIndex = 1;
        answer.forEach(ans => {
          this.seeElement(locate(`${questionRow}/td/span//tr[${ansIndex}]`).withText(ans));
          ansIndex++;
        });
      } else {
        this.seeElement(locate(`${questionRow}/td/span`).withText(answer));
      }
    },

    signOut() {
      this.click('Sign Out');
      this.wait(2); // in seconds
    },

    navigateToCaseDetails(caseId) {
      this.amOnPage(`${baseUrl}/case/${config.definition.jurisdiction}/${config.definition.caseType}/${caseId.replace(/\D/g, '')}`);
      this.waitForText('Sign Out');
    },
  });
};
