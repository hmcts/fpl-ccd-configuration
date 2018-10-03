const config = require('../config.js');

Feature('Enter international elements');

Before((I, caseViewPage) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
	I.waitForElement('.tabs', 10);
	caseViewPage.goToNewActions(config.applicationActions.enterInternationalElement);
	I.waitForElement('ccd-case-edit-page', 10);
});

Scenario('leaving fields empty in international elements of the c110a application', (I) => {
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.see(`updated with event: ${config.applicationActions.enterInternationalElement}`);
});

Scenario('completing half of the international elements section of the c110a application', (I, enterInternationalElementsPage) => {
	enterInternationalElementsPage.halfFillForm();
	I.see('Give reason');
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.see(`updated with event: ${config.applicationActions.enterInternationalElement}`);
});

Scenario('completed international elements of the c110a application', (I, enterInternationalElementsPage) => {
	enterInternationalElementsPage.fillForm();
	I.see('Give reason');
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.see(`updated with event: ${config.applicationActions.enterInternationalElement}`);
});
