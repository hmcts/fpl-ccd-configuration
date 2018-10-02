const config = require('../config.js');

Feature('Next Step Dropdown');

Before((I) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
});

Scenario('See case tab overview', (I, caseViewPage) => {
	I.wait(3);
	I.see(caseViewPage.tabs.ordersHearingTab);
});

Scenario(`Test ${config.applicationActions.selectOrders}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.selectOrders);
	I.see(config.applicationActions.selectOrders);
});

Scenario(`Test ${config.applicationActions.selectHearing}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.selectHearing);
	I.see(config.applicationActions.selectHearing);
});

Scenario(`Test ${config.applicationActions.enterChildren}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.enterChildren);
	I.see(config.applicationActions.enterChildren);
});

Scenario(`Test ${config.applicationActions.enterRespondents}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.enterRespondents);
	I.see(config.applicationActions.enterRespondents);
});

Scenario(`Test ${config.applicationActions.enterApplicants}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.enterApplicants);
	I.see(config.applicationActions.enterApplicants);
});

Scenario(`Test ${config.applicationActions.enterOthers}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.enterOthers);
	I.see(config.applicationActions.enterOthers);
});

Scenario(`Test ${config.applicationActions.enterGrounds}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
	I.see(config.applicationActions.enterGrounds);
});

Scenario(`Test ${config.applicationActions.enterRisk}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.enterRisk);
	I.see(config.applicationActions.enterRisk);
});

Scenario(`Test ${config.applicationActions.enterParentingFactors}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.enterParentingFactors);
	I.see(config.applicationActions.enterParentingFactors);
});

Scenario(`Test ${config.applicationActions.enterInternationalElement}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.enterInternationalElement);
	I.see(config.applicationActions.enterInternationalElement);
});

Scenario(`Test ${config.applicationActions.enterOtherProceedings}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.enterOtherProceedings);
	I.see(config.applicationActions.enterOtherProceedings);
});

Scenario(`Test ${config.applicationActions.enterAllocationProposal}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.enterAllocationProposal);
	I.see(config.applicationActions.enterAllocationProposal);
});

Scenario(`Test ${config.applicationActions.attendingHearing}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.attendingHearing);
	I.see(config.applicationActions.attendingHearing);
});

Scenario(`Test ${config.applicationActions.uploadDocuments}`, (I, caseViewPage) => {
	caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
	I.see(config.applicationActions.uploadDocuments);
});




