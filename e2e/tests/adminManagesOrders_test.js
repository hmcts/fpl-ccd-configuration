const config = require('../config.js');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');
const caseData = require('../fixtures/caseData/gatekeepingFullDetails.json');

const approvalDate = {year: 2021, month: 4, day: 9};
const allocatedJudge = {title: 'Her Honour Judge', name: 'Moley'};
const orderTitle = 'some title';
let caseId;

Feature('HMCTS Admin manages orders');

BeforeSuite(async ({I}) => caseId = await I.submitNewCaseWithData(caseData));

Before(async ({I}) => await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId));

Scenario('Create C32 care order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c32);
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'C32 - Care order',
    approvalDate: approvalDate,
    allocatedJudge: allocatedJudge,
    children: 'Timothy Jones',
  });
});

Scenario('Create C21 blank order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c21);
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterTitle(orderTitle);
  await manageOrdersEventPage.enterDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 2,
    orderType: 'C21 - Blank order',
    orderTitle: orderTitle,
    approvalDate: approvalDate,
    allocatedJudge: allocatedJudge,
    children: 'Timothy Jones',
  });
});

function assertOrder(I, caseViewPage, order) {
  const orderElement = `Order ${order.orderIndex}`;
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab([orderElement, 'Type of order'], order.orderType);
  I.seeInTab([orderElement, 'Approval date'], dateFormat(dateToString(order.approvalDate), 'd mmm yyyy'));
  I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], order.allocatedJudge);
  I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Last name'], order.allocatedJudge);
  I.seeInTab([orderElement, 'Children'], order.children);

  if (order.title !== undefined) {
    I.seeInTab([orderElement, 'Order title'], orderTitle);
  }
}
