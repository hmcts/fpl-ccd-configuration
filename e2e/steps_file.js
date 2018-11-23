/* global locate */
const config = require('./config');

const logIn = require('./pages/login/loginPage');
const createCase = require('./pages/createCase/createCase');
const addEventDetails = require('./pages/createCase/addEventSummary');

'use strict';

module.exports = function () {
  return actor({
    logInAndCreateCase(username, password) {
      logIn.signIn(username, password);
      this.click('Create Case');
      this.waitForElement(`#cc-jurisdiction > option[value="${config.definition.jurisdiction}"]`);
      createCase.createNewCase();
      this.waitForElement('.check-your-answers');
      addEventDetails.submitCase();
    },

    continueAndSubmit() {
      this.click('Continue');
      this.waitForElement('.check-your-answers');
      addEventDetails.submitCase();
      this.waitForElement('.tabs');
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
      this.clearCookie();
      this.refreshPage();
    },
  });
};
