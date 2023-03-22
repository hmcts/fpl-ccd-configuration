const config = require('../config.js');
const gatekeepingCaseData = require('../fixtures/caseData/gatekeepingNoAllocatedJudge.json');

let caseId;

Feature('Gatekeeper Case administration after gatekeeping');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(gatekeepingCaseData); }
  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
}

Scenario('Gatekeeper notifies another gatekeeper with a link to the case', async ({I, caseViewPage, notifyGatekeeperEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.notifyGatekeeper);
  await notifyGatekeeperEventPage.enterEmail('gatekeeper@mailnesia.com');
  await I.completeEvent('Save and continue');
});

Scenario('Gatekeeper adds allocated judge @nightlyOnly', async ({I, caseViewPage, allocatedJudgeEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.allocatedJudge);
  await allocatedJudgeEventPage.enterAllocatedJudge('Moley', 'moley@example.com');
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['Allocated Judge', 'Judge or magistrate\'s title'], 'Her Honour Judge');
  I.seeInTab(['Allocated Judge', 'Last name'], 'Moley');
  I.seeInTab(['Allocated Judge', 'Email Address'], 'moley@example.com');
});
