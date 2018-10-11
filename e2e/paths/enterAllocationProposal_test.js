const config = require('../config.js');

Feature('EnterAllocationProposal');

Before((I, caseViewPage) => {
	I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
	caseViewPage.goToNewActions(config.applicationActions.enterAllocationProposal);
});

Scenario('Click president\'s guidance link and select lay justices for allocation proposal', (I, enterAllocationProposalPage) => {
	I.clickHyperlinkAndReturnToOriginalPage('President\'s Guidance', config.presidentsGuidanceUrl, config.otherProposalUrl);
	enterAllocationProposalPage.selectAllocationProposal('Lay justices');
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationProposal);
});

Scenario('Click schedule link and select lay justices for allocation proposal with proposal reason', (I, enterAllocationProposalPage) => {
	I.clickHyperlinkAndReturnToOriginalPage('schedule', config.scheduleUrl, config.otherProposalUrl);
	enterAllocationProposalPage.selectAllocationProposal('Lay justices');
	enterAllocationProposalPage.enterProposalReason('test');
	I.continueAndSubmit(config.eventSummary, config.eventDescription);
	I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationProposal);
});
