const config = require('../config.js');

Feature('Enter Allocation Proposal');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.enterAllocationProposal);
});

Scenario('Click president\'s guidance link and select lay justices for allocation proposal', (I, enterAllocationProposalPage) => {
  I.clickHyperlink('President\'s Guidance', config.presidentsGuidanceUrl);
  I.clickBrowserBack();
  enterAllocationProposalPage.selectAllocationProposal('Lay justices');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationProposal);
});

Scenario('Click schedule link and select lay justices for allocation proposal with proposal reason', (I, enterAllocationProposalPage) => {
  I.clickHyperlink('schedule', config.scheduleUrl);
  I.clickBrowserBack();
  enterAllocationProposalPage.selectAllocationProposal('Lay justices');
  enterAllocationProposalPage.enterProposalReason('test');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationProposal);
});
