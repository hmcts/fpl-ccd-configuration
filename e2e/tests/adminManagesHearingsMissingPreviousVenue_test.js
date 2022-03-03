const config = require('../config.js');
const gatekeepingWithPastHearingDetailsAndMissingVenueId = require('../fixtures/caseData/gatekeepingWithPastHearingDetailsAndMissingVenueId.json');
const gatekeepingWithPastHearingDetails = require('../fixtures/caseData/gatekeepingWithPastHearingDetails.json');


let caseId;

Feature('Hearing administration - handle unexpected missing hearingVenue');

async function setupScenario(I, missingVenueId) {
  if (missingVenueId) {
    caseId = await I.submitNewCaseWithData(gatekeepingWithPastHearingDetailsAndMissingVenueId);
  } else {
    caseId = await I.submitNewCaseWithData(gatekeepingWithPastHearingDetails);
  }
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
}

Scenario('HMCTS admin edits previous hearing without hearing venue', async ({I, caseViewPage, manageHearingsEventPage}) => {
  await setupScenario(I, true);
  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  manageHearingsEventPage.selectAddNewHearing();
  await I.goToNextPage();
  I.dontSee('Last Court');
});

Scenario('HMCTS admin edits previous hearing with hearing venue', async ({I, caseViewPage, manageHearingsEventPage}) => {
  await setupScenario(I, false);
  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  manageHearingsEventPage.selectAddNewHearing();
  await I.goToNextPage();
  I.see('Last court');
});
