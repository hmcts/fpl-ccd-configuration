const config = require('../config.js');

Feature('Enter international elements');

Before((I, caseViewPage) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
	caseViewPage.goToNewActions(config.applicationActions.enterInternationalElement);
});

Scenario('completing half of the international elements section of the c110a application', (I, enterInternationalElementsPage) => {
	enterInternationalElementsPage.halfFillForm();
	I.see('Give reason');
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterInternationalElement);
});

Scenario('completed international elements of the c110a application', (I, enterInternationalElementsPage) => {
	enterInternationalElementsPage.fillForm();
	I.see('Give reason');
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterInternationalElement);
});
