const config = require('../config.js');

Feature('Enter Allocation Proposal');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.enterAllocationProposal);
});

Scenario('Select lay justices for allocation proposal', (I, enterAllocationProposalPage) => {
  enterAllocationProposalPage.selectAllocationProposal('Lay justices');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationProposal);
});

Scenario('select lay justices for allocation proposal with proposal reason', (I, enterAllocationProposalPage) => {
  enterAllocationProposalPage.selectAllocationProposal('Lay justices');
  enterAllocationProposalPage.enterProposalReason('test');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationProposal);
});

// TODO: Explore external navigation when running in headless mode. Test currently fails when Puppeteer: { show: false }.
// Logic has been extracted to allow for FPL-13 to pass.
xScenario('Click president\'s guidance link and schedule link', (I) => {
  I.clickHyperlink('President\'s Guidance', config.presidentsGuidanceUrl);
  I.clickBrowserBack();
  I.clickHyperlink('schedule', config.scheduleUrl);
  I.clickBrowserBack();
});
