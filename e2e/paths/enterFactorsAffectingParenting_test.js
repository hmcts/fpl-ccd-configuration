const config = require('../config.js');

Feature('EnterFactorsAffectingParenting');

Before((I, caseViewPage) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
	I.waitForElement('.tabs', 10);
	caseViewPage.goToNewActions(config.applicationActions.enterParentingFactors);
	I.waitForElement('ccd-case-edit-page', 5);
});

Scenario('Leaving factors affecting parenting section blank in c110a', (I) => {
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.see(`updated with event: ${config.applicationActions.enterParentingFactors}`);
});

Scenario('Clicking yes to a question produces a textarea in factors affecting parenting section', (I, enterFactorsAffectingParentingPage) => {
	enterFactorsAffectingParentingPage.enterYesForAlcoholOrDrugAbuse();
	I.seeElement('textArea');
});

Scenario('Filling in factors affecting parenting sections of c110a', (I, enterFactorsAffectingParentingPage) => {
	enterFactorsAffectingParentingPage.enterFactorsAffectingParenting();
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.see(`updated with event: ${config.applicationActions.enterParentingFactors}`);
});
