const config = require('../config.js');

const orderFunctions = require('../helpers/generated_order_helper');
const blankOrder = require('../fixtures/orders/blankOrder.js');
const finalHearingCaseData = require('../fixtures/caseData/finalHearingWithMultipleOrders.json');
const moment = require('moment');

Feature('HMCTS super user removes orders');

let caseId;

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(finalHearingCaseData);
  await I.signIn(config.hmctsSuperUser);
});

Before(async ({I}) => await I.navigateToCaseDetailsAs(config.hmctsSuperUser, caseId));

Scenario('HMCTS super user removes a generated order from a case', async ({I, caseViewPage, removeOrderEventPage}) => {
  await caseViewPage.goToNewActions(config.superUserActions.removeOrder);
  let order = finalHearingCaseData.caseData.orderCollection[0];
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

Scenario('HMCTS super user removes a sdo from a case', async ({I, caseViewPage, removeOrderEventPage}) => {
  await caseViewPage.goToNewActions(config.superUserActions.removeOrder);

  let order = finalHearingCaseData.caseData.standardDirectionOrder;
  const labelToSelect = 'Gatekeeping order - ' + moment(order.dateOfIssue).format('D MMMM YYYY');
  removeOrderEventPage.selectOrderToRemove(labelToSelect);
  await I.goToNextPage();
  removeOrderEventPage.addRemoveOrderReason('Entered incorrect order');
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.superUserActions.removeOrder);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  const removeSDO = 'Removed gatekeeping orders 1';

  I.seeInTab([removeSDO, 'File'], 'sdo.pdf');
  I.seeInTab([removeSDO, 'Date of issue'], '28 April 2020');
  I.seeInTab([removeSDO, 'Reason for removal'], 'Entered incorrect order');
});

