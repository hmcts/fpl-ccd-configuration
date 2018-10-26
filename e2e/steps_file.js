/* global locate */
const logIn = require('./pages/login/loginPage');
const createCase = require('./pages/createCase/createCase');
const addEventDetails = require('./pages/createCase/addEventSummary');

'use strict';

module.exports = function () {
  return actor({
    logInAndCreateCase(username, password, summary, description) {
      logIn.signIn(username, password);
      this.waitForNavigation();
      this.click('Create new case');
      createCase.createNewCase();
      this.waitForElement('.check-your-answers');
      addEventDetails.submitCase(summary, description);
      this.waitForElement('.tabs');
    },

    continueAndSubmit(summary, description) {
      this.click('Continue');
      this.waitForElement('.check-your-answers');
      addEventDetails.submitCase(summary, description);
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
      const complexType = locate('div')
        .withAttr({class: 'complex-panel'})
        .withChild('dl')
        .withChild('dt')
        .withChild('span')
        .withText(complexTypeHeading);
      let questionRow = locate(complexType).withChild('table').withChild('tbody').find('tr');
      questionRow = locate(questionRow[questionNo]).withChild('th').withChild('span').withText(question);
      this.seeElement(questionRow.withChild('th').withText(question));
      if (Array.isArray(answer)) {
        answer.forEach(ans => {
          this.seeElement(questionRow.withChild('td').withText(ans));
        });
      } else {
        this.seeElement(questionRow.withChild('td').withText(answer));
      }
    },
  });
};
