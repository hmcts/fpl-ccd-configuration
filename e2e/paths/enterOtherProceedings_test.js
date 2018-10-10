const config = require('../config.js');
const otherProceedingData = require('../fixtures/otherProceedingData');

Feature('Enter Other Proceedings');

Before((I, caseViewPage) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
	caseViewPage.goToNewActions(config.applicationActions.enterOtherProceedings);
});

Scenario('Select not aware of any ongoing or previous proceedings', (I, enterOtherProceedingsPage) => {
	enterOtherProceedingsPage.selectNoForProceeding();
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterOtherProceedings);
});

Scenario('Select yes for ongoing or previous proceedings and fill in information', (I, enterOtherProceedingsPage) => {
	enterOtherProceedingsPage.selectYesForProceeding();
	enterOtherProceedingsPage.enterProceedingInformation(otherProceedingData);
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterOtherProceedings);
});


