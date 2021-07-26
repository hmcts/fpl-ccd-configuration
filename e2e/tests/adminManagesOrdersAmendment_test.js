const config = require('../config.js');
const dateFormat = require('dateformat');
const api = require('../helpers/api_helper');
const caseData = require('../fixtures/caseData/caseWithAllTypesOfOrders.json');
const closedCaseData = {
  state: 'CLOSED',
  caseData: caseData.caseData,
};

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

async function setupScenario(I, data = caseData) {
  if (!caseId || 'CLOSED' === data.state) {
    caseId = await I.submitNewCaseWithData(data);
  }
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
}

Scenario('Amend generated order', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I);
  await amendOrder(I, caseViewPage, manageOrdersEventPage, orders.generated);
  assertAmendment(I, caseViewPage, orders.generated);
  await api.pollLastEvent(caseId, config.internalActions.updateCase);
});

Scenario('Amend standard directions order', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I);
  await amendOrder(I, caseViewPage, manageOrdersEventPage, orders.standardDirectionOrder);
  assertAmendment(I, caseViewPage, orders.standardDirectionOrder);
});

Scenario('Amend urgent hearing order', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I);
  await amendOrder(I, caseViewPage, manageOrdersEventPage, orders.urgentHearingOrder);
  assertAmendment(I, caseViewPage, orders.urgentHearingOrder);
  await api.pollLastEvent(caseId, config.internalActions.updateCase);
});

Scenario('Amend case management order', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I);
  await amendOrder(I, caseViewPage, manageOrdersEventPage, orders.caseManagementOrder);
  assertAmendment(I, caseViewPage, orders.caseManagementOrder);
  await api.pollLastEvent(caseId, config.internalActions.updateCase);
});

Scenario('Amend generated order (closed)', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, closedCaseData);
  await amendOrder(I, caseViewPage, manageOrdersEventPage, orders.generated, manageOrdersEventPage.selectOperationInClosedState);
  assertAmendment(I, caseViewPage, orders.generated);
});

async function amendOrder(I, caseViewPage, manageOrdersEventPage, order, orderOperation = manageOrdersEventPage.selectOperation) {
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);
  await orderOperation(manageOrdersEventPage.operations.options.amend);
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
