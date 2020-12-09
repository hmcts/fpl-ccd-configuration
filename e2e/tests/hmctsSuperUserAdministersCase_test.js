const config = require('../config.js');

const orderCaseData = require('../fixtures/caseData/gatekeepingNoHearingDetails');
const caseManagementCaseData = require('../fixtures/caseData/prepareForHearing.json');
const closedCaseData = require('../fixtures/caseData/closedCase.json');

Feature('Case administration by super user');

let caseId;

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(orderCaseData);
  await I.signIn(config.hmctsSuperUser);
});

Before(async ({I}) => await I.navigateToCaseDetails(caseId));

Scenario('HMCTS super user updates FamilyMan reference number', async ({I, caseViewPage, enterFamilyManCaseNumberEventPage}) => {
  I.seeFamilyManNumber('mockcaseID');
  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  enterFamilyManCaseNumberEventPage.enterCaseID('newMockCaseID');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);
  I.seeFamilyManNumber('newMockCaseID');
});

Scenario('HMCTS super user changes state from case management to final hearing', async ({I, caseViewPage, changeCaseStateEventPage}) => {
  const newCaseId = await I.submitNewCaseWithData(caseManagementCaseData);
  await I.navigateToCaseDetailsAs(config.hmctsSuperUser, newCaseId);

  await caseViewPage.goToNewActions(config.superUserActions.changeCaseState);
  changeCaseStateEventPage.seeAsCurrentState('Case management');
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
  changeCaseStateEventPage.seeAsCurrentState('Closed');
  changeCaseStateEventPage.selectFinalHearing();
  await I.completeEvent(changeCaseStateEventPage.fields.endButton, {summary: 'change state', description: 'change state to final hearing'});
  I.seeEventSubmissionConfirmation(config.superUserActions.changeCaseState);

  caseViewPage.selectTab(caseViewPage.tabs.history);
  I.seeEndStateForEvent(config.superUserActions.changeCaseState, 'Final hearing');
});
