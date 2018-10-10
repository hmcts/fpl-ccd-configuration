const logIn = require('./pages/login/loginPage');
const createCase = require('./pages/createCase/createCase');
const addEventDetails = require('./pages/createCase/addEventSummary');

'use strict';

module.exports = function () {
  return actor({
    logInAndCreateCase(username, password, summary, description) {
      logIn.signIn(username, password);
      this.wait(3);
      this.click('Create new case');
      createCase.createNewCase();
      this.waitForElement('.check-your-answers');
      addEventDetails.submitCase(summary, description);
      this.waitForElement('.tabs', 10);
    },

    continueAndSubmit(summary, description) {
      this.click('Continue');
      this.waitForElement('.check-your-answers', 10);
      addEventDetails.submitCase(summary, description);
      this.waitForElement('.tabs', 10);
    },

    seeEventSubmissionConfirmation(event) {
      this.see(`updated with event: ${event}`);
    }
  });
};
