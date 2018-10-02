const config = require('../config.js');

Feature('Enter international elements');

Before((I, caseViewPage) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
	I.waitForElement('.tabs', 10);
	caseViewPage.goToNewActions(config.applicationActions.enterInternationalElement);
	I.waitForElement('ccd-case-edit-page', 10);
});

Scenario('test form save with no details entered', (I) => {
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.see(`updated with event: ${config.applicationActions.enterInternationalElement}`);
});

Scenario('test half form filled', (I, enterInternationalElementsPage) => {
	enterInternationalElementsPage.halfFillForm();
	I.see('Give reason');
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.see(`updated with event: ${config.applicationActions.selectHearing}`);
});

Scenario('test form is fully filled in', (I, enterInternationalElementsPage) => {
	enterInternationalElementsPage.fillForm();
	I.see('Give reason');
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.see(`updated with event: ${config.applicationActions.selectHearing}`);
});
