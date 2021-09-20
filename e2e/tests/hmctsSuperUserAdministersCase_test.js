const config = require('../config.js');

const caseManagementCaseData = require('../fixtures/caseData/prepareForHearing.json');
const closedCaseData = require('../fixtures/caseData/closedCase.json');

let caseId;

Feature('Case administration by super user');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(caseManagementCaseData); }
  await I.navigateToCaseDetailsAs(config.hmctsSuperUser, caseId);
}

Scenario('HMCTS super user can add gatekeeping order', async ({I, caseViewPage}) => {
  await setupScenario(I);
  await caseViewPage.checkActionsAreAvailable([config.administrationActions.addGatekeepingOrder]);
});

Scenario('HMCTS super user updates case name', async ({I, caseViewPage, changeCaseNameEventPage}) => {
  await setupScenario(I);
  caseViewPage.seeInCaseTitle('e2e test case');

  await caseViewPage.goToNewActions(config.administrationActions.changeCaseName);
  await changeCaseNameEventPage.changeCaseName('e2e test case updated by superuser');
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  await I.seeEventSubmissionConfirmation(config.administrationActions.changeCaseName);
  caseViewPage.seeInCaseTitle('e2e test case updated by superuser');
});

Scenario('HMCTS super user updates FamilyMan reference number', async ({I, caseViewPage, enterFamilyManCaseNumberEventPage}) => {
  await setupScenario(I);
  I.seeFamilyManNumber('mockcaseID');

  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  await enterFamilyManCaseNumberEventPage.enterCaseID('newMockCaseID');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);

  I.seeFamilyManNumber('newMockCaseID');
});

Scenario('HMCTS super user changes state from case management to final hearing', async ({I, caseViewPage, changeCaseStateEventPage}) => {
  await setupScenario(I);

  await caseViewPage.goToNewActions(config.superUserActions.changeCaseState);
  await changeCaseStateEventPage.seeAsCurrentState('Case management');
  changeCaseStateEventPage.changeState();
  await I.completeEvent(changeCaseStateEventPage.fields.endButton, {summary: 'change state', description: 'change state to final hearing'});
  I.seeEventSubmissionConfirmation(config.superUserActions.changeCaseState);

  caseViewPage.selectTab(caseViewPage.tabs.history);
  I.seeEndStateForEvent(config.superUserActions.changeCaseState, 'Final hearing');
});

Scenario('HMCTS super user changes state from closed to final hearing', async ({I, caseViewPage, changeCaseStateEventPage}) => {
  const newCaseId = await I.submitNewCaseWithData(closedCaseData);
  await I.navigateToCaseDetailsAs(config.hmctsSuperUser, newCaseId);

  await caseViewPage.goToNewActions(config.superUserActions.changeCaseState);
  await changeCaseStateEventPage.seeAsCurrentState('Closed');
  changeCaseStateEventPage.selectFinalHearing();
  await I.completeEvent(changeCaseStateEventPage.fields.endButton, {summary: 'change state', description: 'change state to final hearing'});
  I.seeEventSubmissionConfirmation(config.superUserActions.changeCaseState);

  caseViewPage.selectTab(caseViewPage.tabs.history);
  I.seeEndStateForEvent(config.superUserActions.changeCaseState, 'Final hearing');
});
