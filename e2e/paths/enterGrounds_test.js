const config = require('../config.js');

Feature('EnterGrounds');

Before((I) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventSummary);
});

Scenario('Filling in grounds for application section of c110a', (I, caseViewPage, enterGroundsPage) => {
	caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
	enterGroundsPage.enterThresholdCriteriaDetails();
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterGrounds);
});
