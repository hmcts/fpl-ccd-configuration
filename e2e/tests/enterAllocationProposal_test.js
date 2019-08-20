const config = require('../config.js');

Feature('Enter Allocation Proposal');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterAllocationProposal);
});

Scenario('Select lay justices for allocation proposal', (I, enterAllocationProposalEventPage) => {
  enterAllocationProposalEventPage.selectAllocationProposal('Lay justices');
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationProposal);
});

Scenario('Select lay justices for allocation proposal with proposal reason', (I, enterAllocationProposalEventPage) => {
  enterAllocationProposalEventPage.selectAllocationProposal('Lay justices');
  enterAllocationProposalEventPage.enterProposalReason('test');
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationProposal);
});

// TODO: Explore external navigation when running in headless mode. Test currently fails when Puppeteer: { show: false }.
// Logic has been extracted to allow for FPL-13 to pass.
xScenario('Clicking president\'s guidance and schedule link', (I) => {
  I.clickHyperlink('President\'s Guidance', config.presidentsGuidanceUrl);
  I.clickBrowserBack();
  I.clickHyperlink('schedule', config.scheduleUrl);
  I.clickBrowserBack();
});
