const config = require('../config.js');
const dateFormat = require('dateformat');
const caseData = require('../fixtures/caseData/caseWithAllTypesOfOrders.json');

// most file names are overridden to the below values in api_helper
const orders = {
  generated: {
    name: 'C32 - Care order - 7 July 2021',
    file: 'C32 - Care order.pdf',
    tabObjectName: 'Order 4',
    tabOrderDocFieldName: 'Order document',
  },
  standardDirectionOrder: {
    name: 'Gatekeeping order - 4 July 2021',
    file: 'sdo.pdf',
    tabObjectName: 'Gatekeeping order',
    tabOrderDocFieldName: 'File',
  },
  urgentHearingOrder: {
    name: 'Urgent hearing order - 3 July 2021',
    file: 'uho.pdf',
    tabObjectName: 'Gatekeeping order - urgent hearing order',
    tabOrderDocFieldName: 'Order',
  },
  caseManagementOrder: {
    name: 'Sealed case management order issued on 6 July 2021',
    file: 'mockFile.pdf',
    tabObjectName: 'Sealed Case Management Order 1',
    tabOrderDocFieldName: 'Order',
  },
};

let caseId;

Feature('HMCTS Admin amends orders');

async function setupScenario(I, caseViewPage) {
  if (!caseId) {
    caseId = await I.submitNewCaseWithData(caseData);
  }
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);
}

Scenario('Amend generated order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await setupScenario(I, caseViewPage);
  await amendOrder(I, manageOrdersEventPage, orders.generated);
  assertAmendment(I, caseViewPage, orders.generated);
});

Scenario('Amend standard directions order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await setupScenario(I, caseViewPage);
  await amendOrder(I, manageOrdersEventPage, orders.standardDirectionOrder);
  assertAmendment(I, caseViewPage, orders.standardDirectionOrder);
});

Scenario('Amend urgent hearing order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await setupScenario(I, caseViewPage);
  await amendOrder(I, manageOrdersEventPage, orders.urgentHearingOrder);
  assertAmendment(I, caseViewPage, orders.urgentHearingOrder);
});

Scenario('Amend case management order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await setupScenario(I, caseViewPage);
  await amendOrder(I, manageOrdersEventPage, orders.caseManagementOrder);
  assertAmendment(I, caseViewPage, orders.caseManagementOrder);
});

async function amendOrder(I, manageOrdersEventPage, order) {
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.amend);
  manageOrdersEventPage.selectOrderToAmend(order.name);
  await I.goToNextPage();
  manageOrdersEventPage.reviewOrderToAmend(order.file);
  await I.runAccessibilityTest();
  await I.goToNextPage();
  manageOrdersEventPage.uploadAmendedOrder(config.testPdfFile);
  await I.runAccessibilityTest();
  await I.completeEvent('Save and continue');
}

function assertAmendment(I, caseViewPage, order) {
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab([order.tabObjectName, 'Amended'], dateFormat(new Date(), 'd mmm yyyy'));
  I.seeInTab([order.tabObjectName, order.tabOrderDocFieldName], `amended_${order.file}`);
}
