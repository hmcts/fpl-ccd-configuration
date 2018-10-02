const config = require('../config.js');
const addEventDetails = require('../pages/createCase/addEventSummary');


Feature('EnterGrounds');

Before((I) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventSummary);
});

Scenario('Filling in grounds for application section of c110a', (I, caseViewPage, enterGroundsPage) => {
	caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
	I.wait(2);
	enterGroundsPage.enterThresholdCriteriaDetails();
	I.wait(2);
	addEventDetails.submitCase(config.eventSummary, config.eventDescription);
	I.wait(2);
	I.see(`updated with event: ${config.applicationActions.enterGrounds}`);
});
