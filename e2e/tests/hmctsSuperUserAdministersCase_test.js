const config = require('../config.js');

const orderCaseData = require('../fixtures/caseData/gatekeepingNoHearingDetails');
const caseManagementCaseData = require('../fixtures/caseData/prepareForHearing.json');
const closedCaseData = require('../fixtures/caseData/closedCase.json');
const orderFunctions = require('../helpers/generated_order_helper');
const blankOrder = require('../fixtures/orders/blankOrder.js');
const finalHearingCaseData = require('../fixtures/caseData/finalHearing.json');
const moment = require('moment');

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

Scenario('HMCTS super user removes a generated order from a case', async ({I, caseViewPage, removeOrderEventPage}) => {
  await caseViewPage.goToNewActions(config.superUserActions.removeOrder);
  let order = orderCaseData.caseData.orderCollection[0];
  const labelToSelect = order.value.title + ' - ' + order.value.dateOfIssue;
  removeOrderEventPage.selectOrderToRemove(labelToSelect);
  await I.goToNextPage();
  removeOrderEventPage.addRemoveOrderReason('Entered incorrect order');
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.superUserActions.removeOrder);
  const issuedDate = new Date(2020, 4, 26, 14, 33);
  order = {
    ...blankOrder,
    document: 'Blank order (C21).pdf',
  };
  await orderFunctions.assertOrder(I, caseViewPage, order, issuedDate, false, true);
});

Scenario('HMCTS super user removes a cmo from a case', async ({I, caseViewPage, removeOrderEventPage}) => {
  const newCaseId = await I.submitNewCaseWithData(finalHearingCaseData);

  await I.signIn(config.hmctsSuperUser);
  await I.navigateToCaseDetailsAs(config.hmctsSuperUser, newCaseId);

  await caseViewPage.goToNewActions(config.superUserActions.removeOrder);

  let order = finalHearingCaseData.caseData.sealedCMOs[0].value;
  const labelToSelect = 'Case management order - ' + moment(order.dateIssued).format('D MMMM YYYY');
  removeOrderEventPage.selectOrderToRemove(labelToSelect);
  await I.goToNextPage();
  removeOrderEventPage.addRemoveOrderReason('Entered incorrect order');
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.superUserActions.removeOrder);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  const sealedCMO = 'Removed case management orders 1';

  I.seeInTab([sealedCMO, 'Order'], 'mockFile.pdf');
  I.seeInTab([sealedCMO, 'Hearing'], 'Case management hearing, 1 January 2020');
  I.seeInTab([sealedCMO, 'Date sent'], '27 Aug 2020');
  I.seeInTab([sealedCMO, 'Date issued'], '27 Aug 2020');
  I.seeInTab([sealedCMO, 'Judge'], 'Her Honour Judge Reed');
  I.seeInTab([sealedCMO, 'Reason for removal'], 'Entered incorrect order');
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
