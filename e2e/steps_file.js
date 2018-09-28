const logIn = require('./pages/login/loginPage');
const createCase = require('./pages/createCase/createCase');
const addEventDetails = require('./pages/createCase/addEventSummary');

'use strict';

module.exports = function() {
  return actor({
      logInAndCreateCase(username, password, summary, description) {
        logIn.signIn(username, password);
        this.wait(3);
        this.click('Create new case');
        createCase.createNewCase();
        this.wait(3);
        addEventDetails.submitCase(summary, description);
      }
  });
};
