const config = require('../config.js');

Feature('enter children in application');

Before((I, caseViewPage) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
	caseViewPage.goToNewActions(config.applicationActions.enterChildren);
});

Scenario('completing half of the enter children in the c110a application', (I, enterChildrenPage) => {
	enterChildrenPage.halfFillForm();
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterChildren);
});

Scenario('completing entering child information in the c110a application', (I, enterChildrenPage) => {
	enterChildrenPage.fillForm();
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterChildren);
});
