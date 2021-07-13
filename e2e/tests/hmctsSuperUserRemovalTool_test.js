const config = require('../config.js');

const finalHearingCaseData = require('../fixtures/caseData/finalHearingWithMultipleOrders.json');
const caseDataWithApplication = require('../fixtures/caseData/gatekeepingWithPastHearingDetailsAndApplication.json');
const moment = require('moment');

Feature('HMCTS super user removes orders');

let caseId;

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(finalHearingCaseData); }
  await I.navigateToCaseDetailsAs(config.hmctsSuperUser, caseId);
}

Scenario('HMCTS super user removes a generated order through \'Create or upload an order - legacy\' from a case', async ({I, caseViewPage, removalToolEventPage}) => {
  await setupScenario(I);
  const orderToRemove = finalHearingCaseData.caseData.orderCollection[0];
  const labelToSelect = `${orderToRemove.value.title} - ${orderToRemove.value.dateOfIssue}`;

  await removeOrder(I, caseViewPage, removalToolEventPage, labelToSelect, true);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  const generatedOrders = 'Other removed orders 1';
  I.seeInTab([generatedOrders, 'Order title'], 'Example Order Title');
  I.seeInTab([generatedOrders, 'Order document'], 'Blank order (C21).pdf');
  I.seeInTab([generatedOrders, 'Starts on'], '26 May 2020');
  I.seeInTab([generatedOrders, 'Date and time of upload'], '2:33pm, 26 May 2020');
  I.seeInTab([generatedOrders, 'Reason for removal'], 'Entered incorrect order');
});

Scenario('HMCTS super user removes a generated order through \'Manage orders\' from a case', async ({I, caseViewPage, removalToolEventPage}) => {
  await setupScenario(I);
  const orderToRemove = finalHearingCaseData.caseData.orderCollection[1];
  const labelToSelect = `${orderToRemove.value.type} - ` +  moment.utc(orderToRemove.value.dateTimeIssued).format('D MMMM YYYY');

  await removeOrder(I, caseViewPage, removalToolEventPage, labelToSelect, true);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  const generatedOrders = 'Other removed orders 2';
  I.seeInTab([generatedOrders, 'Type of order'], 'Discharge of parental responsibility (C45B)');
  I.seeInTab([generatedOrders, 'Order document'], 'Discharge of parental responsibility (C45B).pdf');
  I.seeInTab([generatedOrders, 'Approval date'], '14 Jun 2021');
  I.seeInTab([generatedOrders, 'Children'], 'Timothy Jones');
  I.seeInTab([generatedOrders, 'Reason for removal'], 'Entered incorrect order');
});


Scenario('HMCTS super user removes a sealed cmo from a case', async ({I, caseViewPage, removalToolEventPage}) => {
  await setupScenario(I);
  const orderToRemove = finalHearingCaseData.caseData.sealedCMOs[0].value;
  const labelToSelect = 'Sealed case management order issued on ' + moment(orderToRemove.dateIssued).format('D MMMM YYYY');

  await removeOrder(I, caseViewPage, removalToolEventPage, labelToSelect, true);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  const sealedCMO = 'Removed case management orders 1';
  I.seeInTab([sealedCMO, 'Order'], 'mockFile.pdf');
  I.seeInTab([sealedCMO, 'Hearing'], 'Case management hearing, 1 January 2020');
  I.seeInTab([sealedCMO, 'Date sent'], '27 Aug 2020');
  I.seeInTab([sealedCMO, 'Date issued'], '27 Aug 2020');
  I.seeInTab([sealedCMO, 'Judge'], 'Her Honour Judge Reed');
  I.seeInTab([sealedCMO, 'Reason for removal'], 'Entered incorrect order');
});

Scenario('HMCTS super user removes a sdo from a case', async ({I, caseViewPage, removalToolEventPage}) => {
  await setupScenario(I);
  const orderToRemove = finalHearingCaseData.caseData.standardDirectionOrder;
  const labelToSelect = `Gatekeeping order - ${moment(orderToRemove.dateOfIssue, 'DDMMMMY').format('D MMMM YYYY')}`;

  await removeOrder(I, caseViewPage, removalToolEventPage, labelToSelect, true);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  const removeSDO = 'Removed gatekeeping orders 1';
  I.seeInTab([removeSDO, 'File'], 'sdo.pdf');
  I.seeInTab([removeSDO, 'Date of issue'], '28 April 2020');
  I.seeInTab([removeSDO, 'Reason for removal'], 'Entered incorrect order');
});

Scenario('HMCTS super user removes a draft cmo from a case', async ({I, caseViewPage, removalToolEventPage}) => {
  await setupScenario(I);
  const orderToRemove = finalHearingCaseData.caseData.hearingOrdersBundlesDrafts[0].value.orders[0].value;
  const labelToSelect = 'Draft case management order sent on ' + moment(orderToRemove.dateSent).format('D MMMM YYYY');

  await removeOrder(I, caseViewPage, removalToolEventPage, labelToSelect, false);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.dontSeeInTab('Removed case management orders 2');
});

Scenario('HMCTS super user removes an agreed cmo from a case', async ({I, caseViewPage, removalToolEventPage}) => {
  await setupScenario(I);
  const orderToRemove = finalHearingCaseData.caseData.hearingOrdersBundlesDrafts[0].value.orders[0].value;
  const labelToSelect = 'Agreed case management order sent on ' + moment(orderToRemove.dateSent).format('D MMMM YYYY');

  await removeOrder(I, caseViewPage, removalToolEventPage, labelToSelect, false);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.dontSeeInTab('Removed case management orders 2');
});


Scenario('HMCTS super user removes a draft order from a case', async ({I, caseViewPage, removalToolEventPage}) => {
  await setupScenario(I);
  const orderToRemove = finalHearingCaseData.caseData.hearingOrdersBundlesDrafts[0].value.orders[0].value;
  const labelToSelect = 'Draft order sent on ' + moment(orderToRemove.dateSent).format('D MMMM YYYY');

  await removeOrder(I, caseViewPage, removalToolEventPage, labelToSelect, false);
  caseViewPage.checkTabIsNotPresent(caseViewPage.tabs.draftOrders);
});

Scenario('HMCTS super user removes an application from the case', async ({I, caseViewPage, removalToolEventPage}) => {
  const newCaseId = await I.submitNewCaseWithData(caseDataWithApplication);
  await I.navigateToCaseDetailsAs(config.hmctsSuperUser, newCaseId);

  const applicationToRemove = caseDataWithApplication.caseData.additionalApplicationsBundle[0].value;
  const labelToSelect = 'C2, ' + applicationToRemove.uploadedDateTime;

  await caseViewPage.goToNewActions(config.superUserActions.removeOrdersAndApplications);
  removalToolEventPage.selectApplicationToRemove(labelToSelect);
  await I.runAccessibilityTest();
  await I.goToNextPage();

  removalToolEventPage.addRemoveApplicationReason('Mistakes were made');
  await I.runAccessibilityTest();
  await I.completeEvent('Submit');

  caseViewPage.selectTab(caseViewPage.tabs.otherApplications);
  const removedApplication = 'Removed applications 1';
  I.seeInTab([removedApplication, 'File'], 'mockFile.pdf');
  I.seeInTab([removedApplication, 'Date and time of upload'], '16 June 2021, 11:49am');
  I.seeInTab([removedApplication, 'Reason for removal'], 'Mistakes were made');
});

const removeOrder = async (I, caseViewPage, removalToolEventPage, labelToSelect, reasonFieldExists) => {
  await caseViewPage.goToNewActions(config.superUserActions.removeOrdersAndApplications);
  removalToolEventPage.selectOrderToRemove(labelToSelect);
  await I.runAccessibilityTest();
  await I.goToNextPage();
  if(reasonFieldExists) {
    removalToolEventPage.addRemoveOrderReason('Entered incorrect order');
  }
  await I.runAccessibilityTest();
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.superUserActions.removeOrdersAndApplications);
};

